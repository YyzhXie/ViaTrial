package com.viatrial.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.viatrial.TestDataDirectoryInitializer;
import com.viatrial.entity.Question;
import com.viatrial.entity.QuestionType;
import com.viatrial.entity.Subject;
import com.viatrial.config.DataDirectoryResolver;
import com.viatrial.mapper.QuestionMapper;
import com.viatrial.mapper.QuestionTypeMapper;
import com.viatrial.mapper.SubjectMapper;
import com.viatrial.security.WriteAccessInterceptor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = com.viatrial.Main.class)
@ContextConfiguration(initializers = TestDataDirectoryInitializer.class)
@AutoConfigureMockMvc
class QuestionTypeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SubjectMapper subjectMapper;

    @Autowired
    private QuestionTypeMapper questionTypeMapper;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private DataDirectoryResolver dataDirectoryResolver;

    private String writeToken() {
        return dataDirectoryResolver.getWriteToken();
    }

    @Test
    void shouldAddListAndDeleteQuestionType() throws Exception {
        Subject subject = createSubject();
        String typeName = shortName("__type_api_");
        Long questionTypeId = null;

        try {
            String response = mockMvc.perform(post("/api/v1/question-types")
                            .header(WriteAccessInterceptor.TOKEN_HEADER, writeToken())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"subjectId\":" + subject.getId() + ",\"name\":\"" + typeName + "\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("success"))
                    .andExpect(jsonPath("$.data").isNumber())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            JsonNode jsonNode = objectMapper.readTree(response);
            questionTypeId = jsonNode.get("data").asLong();

            mockMvc.perform(get("/api/v1/question-types").param("subjectId", String.valueOf(subject.getId())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data[?(@.id == " + questionTypeId
                            + " && @.subjectId == " + subject.getId()
                            + " && @.name == '" + typeName + "')]").exists());

            mockMvc.perform(delete("/api/v1/question-types/{id}", questionTypeId)
                            .header(WriteAccessInterceptor.TOKEN_HEADER, writeToken()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(true));

            questionTypeId = null;
        } finally {
            if (questionTypeId != null) {
                questionTypeMapper.deleteById(questionTypeId);
            }
            subjectMapper.deleteById(subject.getId());
        }
    }

    @Test
    void shouldReturnNotFoundWhenSubjectDoesNotExist() throws Exception {
        mockMvc.perform(post("/api/v1/question-types")
                        .header(WriteAccessInterceptor.TOKEN_HEADER, writeToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"subjectId\":999999999,\"name\":\"选择题\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void shouldReturnConflictWhenQuestionTypeNameDuplicatedInSameSubject() throws Exception {
        Subject subject = createSubject();
        QuestionType questionType = createQuestionType(subject.getId());

        try {
            mockMvc.perform(post("/api/v1/question-types")
                            .header(WriteAccessInterceptor.TOKEN_HEADER, writeToken())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"subjectId\":" + subject.getId()
                                    + ",\"name\":\"" + questionType.getName() + "\"}"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value(409));
        } finally {
            questionTypeMapper.deleteById(questionType.getId());
            subjectMapper.deleteById(subject.getId());
        }
    }

    @Test
    void shouldReturnConflictWhenQuestionTypeHasQuestion() throws Exception {
        Subject subject = createSubject();
        QuestionType questionType = createQuestionType(subject.getId());
        Question question = new Question();

        try {
            question.setSubjectId(subject.getId());
            question.setTypeId(questionType.getId());
            question.setContent("__question_api_" + UUID.randomUUID());
            question.setDifficulty(1);
            questionMapper.insert(question);

            mockMvc.perform(delete("/api/v1/question-types/{id}", questionType.getId())
                            .header(WriteAccessInterceptor.TOKEN_HEADER, writeToken()))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value(409));
        } finally {
            if (question.getId() != null) {
                questionMapper.deleteById(question.getId());
            }
            questionTypeMapper.deleteById(questionType.getId());
            subjectMapper.deleteById(subject.getId());
        }
    }

    private Subject createSubject() {
        Subject subject = new Subject();
        subject.setName(shortName("__subject_"));
        subjectMapper.insert(subject);
        return subject;
    }

    private QuestionType createQuestionType(Long subjectId) {
        QuestionType questionType = new QuestionType();
        questionType.setSubjectId(subjectId);
        questionType.setName(shortName("__type_"));
        questionTypeMapper.insert(questionType);
        return questionType;
    }

    private String shortName(String prefix) {
        return prefix + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}
