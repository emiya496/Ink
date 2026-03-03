package com.inkforge.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.inkforge.common.exception.BusinessException;
import com.inkforge.dto.response.TagVO;
import com.inkforge.entity.ContentTag;
import com.inkforge.entity.Tag;
import com.inkforge.mapper.ContentTagMapper;
import com.inkforge.mapper.TagMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TagService {

    private final TagMapper tagMapper;
    private final ContentTagMapper contentTagMapper;

    public TagService(TagMapper tagMapper, ContentTagMapper contentTagMapper) {
        this.tagMapper = tagMapper;
        this.contentTagMapper = contentTagMapper;
    }

    public List<Tag> list(String type) {
        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<Tag>()
                .eq(Tag::getStatus, "正常");
        if (type != null && !type.isEmpty()) {
            wrapper.eq(Tag::getType, type);
        }
        wrapper.orderByAsc(Tag::getTagName);
        return tagMapper.selectList(wrapper);
    }

    public Tag createCustomTag(String tagName, Long userId) {
        // 检查是否已存在
        Tag existing = tagMapper.selectOne(
                new LambdaQueryWrapper<Tag>().eq(Tag::getTagName, tagName));
        if (existing != null) {
            if ("禁用".equals(existing.getStatus())) {
                throw new BusinessException("标签 [" + tagName + "] 已被禁用，无法使用");
            }
            return existing;
        }
        Tag tag = new Tag();
        tag.setTagName(tagName);
        tag.setType("custom");
        tag.setCreateUserId(userId);
        tag.setStatus("正常");
        tagMapper.insert(tag);
        return tag;
    }

    public List<TagVO> getHotTags(int limit) {
        // 按使用次数统计热门标签
        List<Tag> allTags = tagMapper.selectList(
                new LambdaQueryWrapper<Tag>().eq(Tag::getStatus, "正常"));
        return allTags.stream().map(tag -> {
            Long count = contentTagMapper.selectCount(
                    new LambdaQueryWrapper<ContentTag>().eq(ContentTag::getTagId, tag.getId()));
            TagVO vo = new TagVO();
            vo.setId(tag.getId());
            vo.setTagName(tag.getTagName());
            vo.setType(tag.getType());
            vo.setStatus(tag.getStatus());
            vo.setUseCount(count);
            return vo;
        }).sorted((a, b) -> Long.compare(b.getUseCount(), a.getUseCount()))
          .limit(limit)
          .collect(Collectors.toList());
    }

    public List<Tag> getTagsByContentId(Long contentId) {
        List<ContentTag> cts = contentTagMapper.selectList(
                new LambdaQueryWrapper<ContentTag>().eq(ContentTag::getContentId, contentId));
        if (cts.isEmpty()) return List.of();
        List<Long> tagIds = cts.stream().map(ContentTag::getTagId).collect(Collectors.toList());
        return tagMapper.selectBatchIds(tagIds);
    }

    public Tag getById(Long tagId) {
        return tagMapper.selectById(tagId);
    }
}
