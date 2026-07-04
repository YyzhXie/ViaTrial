package com.viatrial.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.viatrial.common.BizException;
import com.viatrial.common.ErrorCode;
import com.viatrial.common.PageResult;
import com.viatrial.dto.request.QuestionAddRequest;
import com.viatrial.dto.request.QuestionPageRequest;
import com.viatrial.dto.response.QuestionResponse;
import com.viatrial.dto.response.TagResponse;
import com.viatrial.entity.Question;
import com.viatrial.entity.QuestionTag;
import com.viatrial.entity.QuestionType;
import com.viatrial.entity.Subject;
import com.viatrial.entity.Tag;
import com.viatrial.mapper.QuestionMapper;
import com.viatrial.mapper.QuestionTagMapper;
import com.viatrial.mapper.QuestionTypeMapper;
import com.viatrial.mapper.SubjectMapper;
import com.viatrial.mapper.TagMapper;
import com.viatrial.service.QuestionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class QuestionServiceImpl implements QuestionService {

    private final QuestionMapper questionMapper;

    private final SubjectMapper subjectMapper;

    private final QuestionTypeMapper questionTypeMapper;

    private final TagMapper tagMapper;

    private final QuestionTagMapper questionTagMapper;

    public QuestionServiceImpl(QuestionMapper questionMapper,
                               SubjectMapper subjectMapper,
                               QuestionTypeMapper questionTypeMapper,
                               TagMapper tagMapper,
                               QuestionTagMapper questionTagMapper) {
        this.questionMapper = questionMapper;
        this.subjectMapper = subjectMapper;
        this.questionTypeMapper = questionTypeMapper;
        this.tagMapper = tagMapper;
        this.questionTagMapper = questionTagMapper;
    }

    @Override
    @Transactional
    public Long addQuestion(QuestionAddRequest request) {
        validateSubjectAndQuestionType(request.getSubjectId(), request.getTypeId());
        List<Long> tagIds = normalizeTagIds(request.getTagIds());
        validateTags(tagIds);

        Question question = new Question();
        question.setSubjectId(request.getSubjectId());
        question.setTypeId(request.getTypeId());
        question.setContent(request.getContent());
        question.setAnswer(request.getAnswer());
        question.setAnalysis(request.getAnalysis());
        question.setImageUrl(request.getImageUrl());
        question.setAnswerImageUrl(request.getAnswerImageUrl());
        question.setDifficulty(request.getDifficulty() == null ? 1 : request.getDifficulty());
        questionMapper.insert(question);

        for (Long tagId : tagIds) {
            QuestionTag questionTag = new QuestionTag();
            questionTag.setQuestionId(question.getId());
            questionTag.setTagId(tagId);
            questionTagMapper.insert(questionTag);
        }
        return question.getId();
    }

    @Override
    @Transactional
    public Boolean updateQuestion(Long id, QuestionAddRequest request) {
        Question question = questionMapper.selectById(id);
        if (question == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "Question does not exist");
        }

        validateSubjectAndQuestionType(request.getSubjectId(), request.getTypeId());
        List<Long> tagIds = normalizeTagIds(request.getTagIds());
        validateTags(tagIds);

        question.setSubjectId(request.getSubjectId());
        question.setTypeId(request.getTypeId());
        question.setContent(request.getContent());
        question.setAnswer(request.getAnswer());
        question.setAnalysis(request.getAnalysis());
        question.setImageUrl(request.getImageUrl());
        question.setAnswerImageUrl(request.getAnswerImageUrl());
        question.setDifficulty(request.getDifficulty() == null ? 1 : request.getDifficulty());
        question.setUpdatedTime(LocalDateTime.now());
        questionMapper.updateById(question);

        questionTagMapper.delete(new QueryWrapper<QuestionTag>().eq("question_id", id));
        for (Long tagId : tagIds) {
            QuestionTag questionTag = new QuestionTag();
            questionTag.setQuestionId(id);
            questionTag.setTagId(tagId);
            questionTagMapper.insert(questionTag);
        }
        return true;
    }

    @Override
    public PageResult<QuestionResponse> pageQuestions(QuestionPageRequest request) {
        QueryWrapper<Question> queryWrapper = new QueryWrapper<Question>().orderByDesc("created_time").orderByDesc("id");
        if (request.getSubjectId() != null) {
            queryWrapper.eq("subject_id", request.getSubjectId());
        }
        if (request.getTypeId() != null) {
            queryWrapper.eq("type_id", request.getTypeId());
        }
        if (request.getTagId() != null) {
            List<Long> questionIds = questionTagMapper.selectList(
                            new QueryWrapper<QuestionTag>().eq("tag_id", request.getTagId()))
                    .stream()
                    .map(QuestionTag::getQuestionId)
                    .toList();
            if (questionIds.isEmpty()) {
                return PageResult.of(0, request.getPage(), request.getSize(), Collections.emptyList());
            }
            queryWrapper.in("id", questionIds);
        }

        Page<Question> page = questionMapper.selectPage(new Page<>(request.getPage(), request.getSize()), queryWrapper);
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), toQuestionResponses(page.getRecords()));
    }

    @Override
    @Transactional
    public Boolean deleteQuestion(Long id) {
        Question question = questionMapper.selectById(id);
        if (question == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "Question does not exist");
        }

        questionTagMapper.delete(new QueryWrapper<QuestionTag>().eq("question_id", id));
        return questionMapper.deleteById(id) > 0;
    }

    public List<QuestionResponse> toQuestionResponses(List<Question> questions) {
        if (questions == null || questions.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> subjectIds = questions.stream().map(Question::getSubjectId).collect(Collectors.toSet());
        Set<Long> typeIds = questions.stream().map(Question::getTypeId).collect(Collectors.toSet());
        Map<Long, Subject> subjectMap = subjectMapper.selectBatchIds(subjectIds)
                .stream()
                .collect(Collectors.toMap(Subject::getId, Function.identity()));
        Map<Long, QuestionType> typeMap = questionTypeMapper.selectBatchIds(typeIds)
                .stream()
                .collect(Collectors.toMap(QuestionType::getId, Function.identity()));
        Map<Long, List<TagResponse>> tagMap = buildTagMap(questions.stream().map(Question::getId).toList());

        List<QuestionResponse> responses = new ArrayList<>();
        for (Question question : questions) {
            QuestionResponse response = new QuestionResponse();
            response.setId(question.getId());
            response.setSubjectId(question.getSubjectId());
            response.setSubjectName(subjectMap.get(question.getSubjectId()).getName());
            response.setTypeId(question.getTypeId());
            response.setTypeName(typeMap.get(question.getTypeId()).getName());
            response.setContent(question.getContent());
            response.setAnswer(question.getAnswer());
            response.setAnalysis(question.getAnalysis());
            response.setImageUrl(question.getImageUrl());
            response.setAnswerImageUrl(question.getAnswerImageUrl());
            response.setDifficulty(question.getDifficulty());
            response.setTags(tagMap.getOrDefault(question.getId(), Collections.emptyList()));
            response.setCreatedTime(question.getCreatedTime());
            responses.add(response);
        }
        return responses;
    }

    private List<Long> normalizeTagIds(List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Long> uniqueTagIds = new HashSet<>(tagIds);
        if (uniqueTagIds.size() != tagIds.size()) {
            throw new BizException(ErrorCode.PARAM_ERROR, "Tag IDs cannot contain duplicates");
        }
        return new ArrayList<>(tagIds);
    }

    private void validateSubjectAndQuestionType(Long subjectId, Long typeId) {
        Subject subject = subjectMapper.selectById(subjectId);
        if (subject == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "Subject does not exist");
        }

        QuestionType questionType = questionTypeMapper.selectById(typeId);
        if (questionType == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "Question type does not exist");
        }
        if (!questionType.getSubjectId().equals(subjectId)) {
            throw new BizException(ErrorCode.PARAM_ERROR, "Question type does not belong to subject");
        }
    }

    private void validateTags(List<Long> tagIds) {
        if (tagIds.isEmpty()) {
            return;
        }
        List<Tag> tags = tagMapper.selectBatchIds(tagIds);
        if (tags.size() != tagIds.size()) {
            throw new BizException(ErrorCode.NOT_FOUND, "Tag does not exist");
        }
    }

    private Map<Long, List<TagResponse>> buildTagMap(List<Long> questionIds) {
        if (questionIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<QuestionTag> questionTags = questionTagMapper.selectList(
                new QueryWrapper<QuestionTag>().in("question_id", questionIds));
        if (questionTags.isEmpty()) {
            return Collections.emptyMap();
        }

        Set<Long> tagIds = questionTags.stream().map(QuestionTag::getTagId).collect(Collectors.toSet());
        Map<Long, Tag> tags = tagMapper.selectBatchIds(tagIds)
                .stream()
                .collect(Collectors.toMap(Tag::getId, Function.identity()));

        return questionTags.stream()
                .collect(Collectors.groupingBy(
                        QuestionTag::getQuestionId,
                        Collectors.mapping(questionTag -> {
                            Tag tag = tags.get(questionTag.getTagId());
                            return new TagResponse(tag.getId(), tag.getName());
                        }, Collectors.toList())));
    }
}
