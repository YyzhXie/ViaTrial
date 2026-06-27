package com.viatrial.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.viatrial.database.DatabaseInitializer;
import com.viatrial.entity.QuestionType;
import com.viatrial.entity.Subject;
import com.viatrial.mapper.QuestionTypeMapper;
import com.viatrial.mapper.SubjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SubjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SubjectMapper subjectMapper;

    @Autowired
    private QuestionTypeMapper questionTypeMapper;

    @BeforeAll
    static void initDatabase() {
        DatabaseInitializer.initialize();
    }

    @Test
    void shouldAddListAndDeleteSubject() throws Exception {
        String name = "__subject_api_" + UUID.randomUUID();
        Long subjectId = null;

        try {
            String response = mockMvc.perform(post("/api/v1/subjects")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"" + name + "\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("success"))
                    .andExpect(jsonPath("$.data").isNumber())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            JsonNode jsonNode = objectMapper.readTree(response);
            subjectId = jsonNode.get("data").asLong();

            mockMvc.perform(get("/api/v1/subjects"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data[?(@.id == " + subjectId + " && @.name == '" + name + "')]").exists());

            mockMvc.perform(delete("/api/v1/subjects/{id}", subjectId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(true));

            subjectId = null;
        } finally {
            if (subjectId != null) {
                subjectMapper.deleteById(subjectId);
            }
            subjectMapper.delete(new QueryWrapper<Subject>().eq("name", name));
        }
    }

    @Test
    void shouldReturnConflictWhenSubjectHasQuestionType() throws Exception {
        String subjectName = "__subject_conflict_" + UUID.randomUUID();
        String typeName = "__type_conflict_" + UUID.randomUUID();
        Subject subject = new Subject();
        subject.setName(subjectName);
        QuestionType questionType = new QuestionType();

        try {
            subjectMapper.insert(subject);

            questionType.setSubjectId(subject.getId());
            questionType.setName(typeName);
            questionTypeMapper.insert(questionType);

            mockMvc.perform(delete("/api/v1/subjects/{id}", subject.getId()))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value(409))
                    .andExpect(jsonPath("$.data").doesNotExist());
        } finally {
            if (questionType.getId() != null) {
                questionTypeMapper.deleteById(questionType.getId());
            }
            if (subject.getId() != null) {
                subjectMapper.deleteById(subject.getId());
            }
        }
    }
}
