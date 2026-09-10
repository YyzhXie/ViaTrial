package com.viatrial.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.viatrial.TestDataDirectoryInitializer;
import com.viatrial.config.DataDirectoryResolver;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 数据安全相关的不变量测试。
 *
 * <p>覆盖审计项：</p>
 * <ul>
 *   <li>A-1：非同源写请求必须携带访问令牌；令牌缺失/错误一律 401，且不会写入数据。</li>
 *   <li>A-4：接口文档默认关闭。</li>
 *   <li>D-4：测试使用 target/ 下的独立数据目录，不污染真实库。</li>
 *   <li>D-5：连接池中每个连接都必须处于 {@code PRAGMA foreign_keys=ON} 状态。</li>
 *   <li>D-1：schema 只从 classpath:schema.sql 加载（重复副本 init.sql 已删除）。</li>
 * </ul>
 */
@SpringBootTest(classes = com.viatrial.Main.class)
@ContextConfiguration(initializers = TestDataDirectoryInitializer.class)
@AutoConfigureMockMvc
class DataSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DataDirectoryResolver dataDirectoryResolver;

    @BeforeAll
    static void verifySchemaSingleSource() {
        assertTrue(DataSecurityTest.class.getResource("/schema.sql") != null, "schema.sql 必须存在");
        assertFalse(DataSecurityTest.class.getResource("/sql/init.sql") != null,
                "重复的 schema 副本 sql/init.sql 必须已删除");
    }

    @Test
    void testDataSourceShouldBeIsolatedFromProductionDatabase() {
        Path dataDirectory = dataDirectoryResolver.getDataDirectory();
        assertTrue(dataDirectory.toString().contains("test-data"),
                "测试数据目录应位于 target/test-data，实际为 " + dataDirectory);
    }

    @Test
    void foreignKeysShouldBeEnabledOnPooledConnections() {
        Integer enabled = jdbcTemplate.queryForObject("PRAGMA foreign_keys", Integer.class);
        assertEquals(1, enabled, "连接池连接必须开启外键约束");
    }

    @Test
    void apiDocsShouldBeDisabledByDefault() throws Exception {
        // 生产配置中 springdoc 默认关闭（审计项 A-4）；这里验证关闭后端点确实不可访问。
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isNotFound());
    }

    @Test
    void sessionEndpointShouldExposeWriteTokenToLocalClients() throws Exception {
        String body = mockMvc.perform(get("/api/v1/system/session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.writeTokenRequired").value(true))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode node = objectMapper.readTree(body);
        assertEquals(dataDirectoryResolver.getWriteToken(), node.get("writeToken").asText());
        assertTrue(Files.exists(dataDirectoryResolver.getWriteTokenFile()), "令牌文件应已持久化");
    }

    @Test
    void crossSiteWriteRequestShouldBeRejected() throws Exception {
        String name = "__security_origin_" + UUID.randomUUID();

        // 跨站来源在 CORS 层即被拒绝（403），请求到不了控制器，更不会写库。
        mockMvc.perform(post("/api/v1/subjects")
                        .header(HttpHeaders.ORIGIN, "http://evil.example.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\"}"))
                .andExpect(status().isForbidden());

        Integer created = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM subject WHERE name = ?", Integer.class, name);
        assertEquals(0, created, "被拒绝的跨站写请求不得写入任何数据");
    }

    @Test
    void writeRequestWithoutTokenShouldBeRejected() throws Exception {
        String name = "__security_notoken_" + UUID.randomUUID();

        mockMvc.perform(post("/api/v1/subjects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));

        Integer created = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM subject WHERE name = ?", Integer.class, name);
        assertEquals(0, created, "缺少令牌的写请求不得写入任何数据");
    }

    @Test
    void writeRequestWithWrongTokenShouldBeRejected() throws Exception {
        String name = "__security_badtoken_" + UUID.randomUUID();

        mockMvc.perform(post("/api/v1/subjects")
                        .header(WriteAccessInterceptor.TOKEN_HEADER, "not-the-right-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));

        Integer created = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM subject WHERE name = ?", Integer.class, name);
        assertEquals(0, created, "令牌错误的写请求不得写入任何数据");
    }

    @Test
    void writeRequestWithTokenShouldSucceedAndBeDeletable() throws Exception {
        String name = "__security_ok_" + UUID.randomUUID();
        Long subjectId = null;

        try {
            String body = mockMvc.perform(post("/api/v1/subjects")
                            .header(WriteAccessInterceptor.TOKEN_HEADER, dataDirectoryResolver.getWriteToken())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"" + name + "\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            subjectId = objectMapper.readTree(body).get("data").asLong();

            mockMvc.perform(delete("/api/v1/subjects/{id}", subjectId)
                            .header(WriteAccessInterceptor.TOKEN_HEADER, dataDirectoryResolver.getWriteToken()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").value(true));

            subjectId = null;
        } finally {
            if (subjectId != null) {
                jdbcTemplate.update("DELETE FROM subject WHERE id = ?", subjectId);
            }
        }
    }

    @Test
    void foreignKeyRestrictionShouldBeEnforcedByDatabase() {
        String subjectName = "__security_fk_subject_" + UUID.randomUUID();
        String tagName = "__security_fk_tag_" + UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO subject (name) VALUES (?)", subjectName);
        jdbcTemplate.update("INSERT INTO tag (name) VALUES (?)", tagName);
        Long subjectId = jdbcTemplate.queryForObject(
                "SELECT id FROM subject WHERE name = ?", Long.class, subjectName);
        Long tagId = jdbcTemplate.queryForObject(
                "SELECT id FROM tag WHERE name = ?", Long.class, tagName);

        try {
            // question_type.subject_id -> subject.id 为 ON DELETE RESTRICT
            jdbcTemplate.update("INSERT INTO question_type (subject_id, name) VALUES (?, ?)",
                    subjectId, "__security_fk_type_" + UUID.randomUUID());

            org.junit.jupiter.api.Assertions.assertThrows(Exception.class,
                    () -> jdbcTemplate.update("DELETE FROM subject WHERE id = ?", subjectId),
                    "存在子表引用时，外键 RESTRICT 必须阻止删除");
        } finally {
            jdbcTemplate.update("DELETE FROM question_type WHERE subject_id = ?", subjectId);
            jdbcTemplate.update("DELETE FROM subject WHERE id = ?", subjectId);
            jdbcTemplate.update("DELETE FROM tag WHERE id = ?", tagId);
        }
    }
}
