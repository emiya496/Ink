package com.inkforge.controller;

import com.inkforge.common.PageResult;
import com.inkforge.common.Result;
import com.inkforge.dto.response.CommentVO;
import com.inkforge.service.CommentService;
import com.inkforge.util.SecurityUtil;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/comment")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public Result<Void> add(@RequestBody Map<String, Object> body) {
        Long contentId = Long.valueOf(body.get("contentId").toString());
        String content = (String) body.get("content");
        Long userId = SecurityUtil.getCurrentUserId();
        commentService.addComment(contentId, userId, content);
        return Result.success();
    }

    @GetMapping("/{contentId}")
    public Result<PageResult<CommentVO>> list(
            @PathVariable Long contentId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        return Result.success(commentService.listByContentId(contentId, page, size));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        String role = SecurityUtil.getCurrentRole();
        commentService.deleteComment(id, userId, role);
        return Result.success();
    }
}
