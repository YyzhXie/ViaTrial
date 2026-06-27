package com.viatrial.service;

import com.viatrial.dto.request.QuestionTypeAddRequest;
import com.viatrial.dto.response.QuestionTypeResponse;

import java.util.List;

public interface QuestionTypeService {

    Long addQuestionType(QuestionTypeAddRequest request);

    List<QuestionTypeResponse> listQuestionTypes(Long subjectId);

    Boolean deleteQuestionType(Long id);
}
