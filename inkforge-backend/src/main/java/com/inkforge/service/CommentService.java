package com.inkforge.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.inkforge.common.PageResult;
import com.inkforge.common.exception.BusinessException;
import com.inkforge.dto.response.CommentVO;
import com.inkforge.entity.Comment;
import com.inkforge.entity.User;
import com.inkforge.mapper.CommentMapper;
import com.inkforge.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {

    private final CommentMapper commentMapper;
    private final UserMapper userMapper;

    public CommentService(CommentMapper commentMapper, UserMapper userMapper) {
        this.commentMapper = commentMapper;
        this.userMapper = userMapper;
    }

    public void addComment(Long contentId, Long userId, String content) {
        if (content == null || content.isBlank()) {
            throw new BusinessException("评论内容不能为空");
        }
        Comment comment = new Comment();
        comment.setContentId(contentId);
        comment.setUserId(userId);
        comment.setContent(content.trim());
        commentMapper.insert(comment);
    }

    public PageResult<CommentVO> listByContentId(Long contentId, Integer page, Integer size) {
        IPage<Comment> pageResult = commentMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<Comment>()
                        .eq(Comment::getContentId, contentId)
                        .orderByDesc(Comment::getCreateTime));
        List<CommentVO> voList = pageResult.getRecords().stream()
                .map(this::toVO).collect(Collectors.toList());
        return PageResult.of(pageResult.getTotal(), voList, page, size);
    }

    public void deleteComment(Long commentId, Long userId, String role) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) throw new BusinessException(404, "评论不存在");
        if (!"admin".equals(role) && !comment.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权删除");
        }
        commentMapper.deleteById(commentId);
    }

    private CommentVO toVO(Comment comment) {
        CommentVO vo = new CommentVO();
        vo.setId(comment.getId());
        vo.setContentId(comment.getContentId());
        vo.setUserId(comment.getUserId());
        vo.setContent(comment.getContent());
        vo.setCreateTime(comment.getCreateTime());
        User user = userMapper.selectById(comment.getUserId());
        if (user != null) {
            vo.setUsername(user.getUsername());
            vo.setAvatar(user.getAvatar());
        }
        return vo;
    }
}
