package com.viatrial.service;

import com.viatrial.dto.request.TagAddRequest;
import com.viatrial.dto.response.TagResponse;

import java.util.List;

public interface TagService {

    Long addTag(TagAddRequest request);

    List<TagResponse> listTags();

    Boolean deleteTag(Long id);
}
