package com.viatrial.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.viatrial.entity.Question;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface QuestionMapper extends BaseMapper<Question> {

    @Select("SELECT * FROM question WHERE subject_id = #{subjectId} ORDER BY RANDOM() LIMIT #{limit}")
    List<Question> selectRandomBySubjectId(@Param("subjectId") Long subjectId, @Param("limit") Integer limit);
}
