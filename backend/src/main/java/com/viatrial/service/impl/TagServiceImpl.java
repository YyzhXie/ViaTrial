package com.viatrial.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.viatrial.common.BizException;
import com.viatrial.common.ErrorCode;
import com.viatrial.dto.request.TagAddRequest;
import com.viatrial.dto.response.TagResponse;
import com.viatrial.entity.QuestionTag;
import com.viatrial.entity.Tag;
import com.viatrial.mapper.QuestionTagMapper;
import com.viatrial.mapper.TagMapper;
import com.viatrial.service.TagService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TagServiceImpl implements TagService {

    private final TagMapper tagMapper;

    private final QuestionTagMapper questionTagMapper;

    public TagServiceImpl(TagMapper tagMapper, QuestionTagMapper questionTagMapper) {
        this.tagMapper = tagMapper;
        this.questionTagMapper = questionTagMapper;
    }

    @Override
    @Transactional
    public Long addTag(TagAddRequest request) {
        String name = request.getName().trim();
        Long count = tagMapper.selectCount(new QueryWrapper<Tag>().eq("name", name));
        if (count > 0) {
            throw new BizException(ErrorCode.CONFLICT, "Tag name already exists");
        }

        Tag tag = new Tag();
        tag.setName(name);
        try {
            tagMapper.insert(tag);
        } catch (DataIntegrityViolationException ex) {
            throw new BizException(ErrorCode.CONFLICT, "Tag name already exists");
        }
        return tag.getId();
    }

    @Override
    public List<TagResponse> listTags() {
        return tagMapper.selectList(new QueryWrapper<Tag>().orderByAsc("id"))
                .stream()
                .map(tag -> new TagResponse(tag.getId(), tag.getName()))
                .toList();
    }

    @Override
    @Transactional
    public Boolean deleteTag(Long id) {
        Tag tag = tagMapper.selectById(id);
        if (tag == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "Tag does not exist");
        }

        Long usageCount = questionTagMapper.selectCount(new QueryWrapper<QuestionTag>().eq("tag_id", id));
        if (usageCount > 0) {
            throw new BizException(ErrorCode.CONFLICT, "Tag is still used by questions");
        }

        return tagMapper.deleteById(id) > 0;
    }
}
