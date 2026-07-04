package com.viatrial.service;

import com.viatrial.common.PageResult;
import com.viatrial.dto.request.QuestionAddRequest;
import com.viatrial.dto.request.QuestionPageRequest;
import com.viatrial.dto.response.QuestionResponse;

public interface QuestionService {

    Long addQuestion(QuestionAddRequest request);

    Boolean updateQuestion(Long id, QuestionAddRequest request);

    PageResult<QuestionResponse> pageQuestions(QuestionPageRequest request);

    Boolean deleteQuestion(Long id);
}
