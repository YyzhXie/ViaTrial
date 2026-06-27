package com.viatrial.service;

import com.viatrial.dto.request.SubjectAddRequest;
import com.viatrial.dto.response.SubjectResponse;

import java.util.List;

public interface SubjectService {

    Long addSubject(SubjectAddRequest request);

    List<SubjectResponse> listSubjects();

    Boolean deleteSubject(Long id);
}
