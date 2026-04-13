package com.inkforge.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.inkforge.common.PageResult;
import com.inkforge.common.exception.BusinessException;
import com.inkforge.entity.*;
import com.inkforge.mapper.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final UserMapper userMapper;
    private final ContentMapper contentMapper;
    private final TagMapper tagMapper;
    private final CommentMapper commentMapper;
    private final AiLogMapper aiLogMapper;

    public AdminService(UserMapper userMapper, ContentMapper contentMapper,
                        TagMapper tagMapper, CommentMapper commentMapper, AiLogMapper aiLogMapper) {
        this.userMapper = userMapper;
        this.contentMapper = contentMapper;
        this.tagMapper = tagMapper;
        this.commentMapper = commentMapper;
        this.aiLogMapper = aiLogMapper;
    }

    // ========== 用户管理 ==========
    public PageResult<User> listUsers(Integer page, Integer size, String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(User::getUsername, keyword);
        }
        wrapper.orderByDesc(User::getCreateTime);
        IPage<User> pageResult = userMapper.selectPage(new Page<>(page, size), wrapper);
        return PageResult.of(pageResult.getTotal(), pageResult.getRecords(), page, size);
    }

    public void updateUserStatus(Long userId, String status) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException(404, "用户不存在");
        if ("admin".equals(user.getRole())) throw new BusinessException("不能修改管理员状态");
        user.setStatus(status);
        userMapper.updateById(user);
    }

    public void deleteUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException(404, "用户不存在");
        if ("admin".equals(user.getRole())) throw new BusinessException("不能删除管理员");
        userMapper.deleteById(userId);
    }

    // ========== 内容管理 ==========
    public PageResult<Content> listContents(Integer page, Integer size, String type, String status, String keyword) {
        LambdaQueryWrapper<Content> wrapper = new LambdaQueryWrapper<>();
        if (type != null && !type.isEmpty()) wrapper.eq(Content::getType, type);
        if (status != null && !status.isEmpty()) wrapper.eq(Content::getStatus, status);
        if (keyword != null && !keyword.isEmpty()) wrapper.like(Content::getTitle, keyword);
        wrapper.orderByDesc(Content::getCreateTime);
        IPage<Content> pageResult = contentMapper.selectPage(new Page<>(page, size), wrapper);
        return PageResult.of(pageResult.getTotal(), pageResult.getRecords(), page, size);
    }

    public void updateContentStatus(Long contentId, String status) {
        Content content = contentMapper.selectById(contentId);
        if (content == null) throw new BusinessException(404, "内容不存在");
        content.setStatus(status);
        contentMapper.updateById(content);
    }

    public void deleteContent(Long contentId) {
        if (contentMapper.selectById(contentId) == null) throw new BusinessException(404, "内容不存在");
        contentMapper.deleteById(contentId);
    }

    // ========== 标签管理 ==========
    public PageResult<Tag> listTags(Integer page, Integer size, String keyword) {
        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<Tag>().orderByDesc(Tag::getCreateTime);
        if (keyword != null && !keyword.isEmpty()) wrapper.like(Tag::getTagName, keyword);
        IPage<Tag> pageResult = tagMapper.selectPage(new Page<>(page, size), wrapper);
        return PageResult.of(pageResult.getTotal(), pageResult.getRecords(), page, size);
    }

    public void createSystemTag(String tagName) {
        Long count = tagMapper.selectCount(
                new LambdaQueryWrapper<Tag>().eq(Tag::getTagName, tagName));
        if (count > 0) throw new BusinessException("标签已存在");
        Tag tag = new Tag();
        tag.setTagName(tagName);
        tag.setType("system");
        tag.setStatus("正常");
        tagMapper.insert(tag);
    }

    public void updateTagStatus(Long tagId, String status) {
        Tag tag = tagMapper.selectById(tagId);
        if (tag == null) throw new BusinessException(404, "标签不存在");
        tag.setStatus(status);
        tagMapper.updateById(tag);
    }

    public void deleteTag(Long tagId) {
        if (tagMapper.selectById(tagId) == null) throw new BusinessException(404, "标签不存在");
        tagMapper.deleteById(tagId);
    }

    // ========== 评论管理 ==========
    public PageResult<Comment> listComments(Integer page, Integer size, String keyword) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<Comment>().orderByDesc(Comment::getCreateTime);
        if (keyword != null && !keyword.isEmpty()) wrapper.like(Comment::getContent, keyword);
        IPage<Comment> pageResult = commentMapper.selectPage(new Page<>(page, size), wrapper);
        return PageResult.of(pageResult.getTotal(), pageResult.getRecords(), page, size);
    }

    public void deleteComment(Long commentId) {
        if (commentMapper.selectById(commentId) == null) throw new BusinessException(404, "评论不存在");
        commentMapper.deleteById(commentId);
    }

    // ========== AI统计 ==========
    public Map<String, Object> getAiStats() {
        List<AiLog> allLogs = aiLogMapper.selectList(null);

        long total = allLogs.size();

        // 按用户统计
        Map<Long, Long> byUser = allLogs.stream()
                .collect(Collectors.groupingBy(AiLog::getUserId, Collectors.counting()));

        // 按功能统计
        Map<String, Long> byFunction = allLogs.stream()
                .collect(Collectors.groupingBy(AiLog::getFunctionType, Collectors.counting()));

        return Map.of(
                "total", total,
                "byUser", byUser,
                "byFunction", byFunction
        );
    }
}
