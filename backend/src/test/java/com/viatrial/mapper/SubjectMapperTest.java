package com.viatrial.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.viatrial.database.DatabaseInitializer;
import com.viatrial.entity.Subject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class SubjectMapperTest {

    @Autowired
    private SubjectMapper subjectMapper;

    @BeforeAll
    static void initDatabase() {
        DatabaseInitializer.initialize();
    }

    @Test
    void shouldInsertSelectAndDeleteSubject() {
        String name = "__mapper_test_" + UUID.randomUUID();

        Subject subject = new Subject();
        subject.setName(name);

        try {
            assertEquals(1, subjectMapper.insert(subject));
            assertNotNull(subject.getId());

            Subject saved = subjectMapper.selectById(subject.getId());
            assertNotNull(saved);
            assertEquals(name, saved.getName());

            Long count = subjectMapper.selectCount(new QueryWrapper<Subject>().eq("name", name));
            assertEquals(1L, count);
        } finally {
            if (subject.getId() != null) {
                subjectMapper.deleteById(subject.getId());
            } else {
                subjectMapper.delete(new QueryWrapper<Subject>().eq("name", name));
            }
        }

        Long countAfterDelete = subjectMapper.selectCount(new QueryWrapper<Subject>().eq("name", name));
        assertEquals(0L, countAfterDelete);
    }
}
