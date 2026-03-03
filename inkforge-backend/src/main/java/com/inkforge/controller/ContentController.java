package com.inkforge.controller;

import com.inkforge.common.PageResult;
import com.inkforge.common.Result;
import com.inkforge.dto.request.ContentCreateRequest;
import com.inkforge.dto.response.ContentVO;
import com.inkforge.service.ContentService;
import com.inkforge.util.SecurityUtil;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/content")
public class ContentController {

    private final ContentService contentService;

    public ContentController(ContentService contentService) {
        this.contentService = contentService;
    }

    @PostMapping("/create")
    public Result<Long> create(@RequestBody ContentCreateRequest req) {
        Long userId = SecurityUtil.getCurrentUserId();
        return Result.success(contentService.create(req, userId));
    }

    @GetMapping("/list")
    public Result<PageResult<ContentVO>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) Long tagId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        return Result.success(contentService.list(page, size, type, keyword, sortBy, tagId, currentUserId));
    }

    @GetMapping("/rank")
    public Result<List<ContentVO>> getRanking(
            @RequestParam(required = false, defaultValue = "") String type,
            @RequestParam(required = false, defaultValue = "reads") String rankType) {
        return Result.success(contentService.getRanking(type, rankType));
    }

    @GetMapping("/hot-banner")
    public Result<ContentVO> getHotBanner(
            @RequestParam(required = false, defaultValue = "") String type) {
        return Result.success(contentService.getHotBanner(type));
    }

    @GetMapping("/{id}")
    public Result<ContentVO> detail(@PathVariable Long id) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        return Result.success(contentService.getDetail(id, currentUserId));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody ContentCreateRequest req) {
        Long userId = SecurityUtil.getCurrentUserId();
        contentService.update(id, req, userId);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        String role = SecurityUtil.getCurrentRole();
        contentService.delete(id, userId, role);
        return Result.success();
    }

    @GetMapping("/my")
    public Result<PageResult<ContentVO>> myContent(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = SecurityUtil.getCurrentUserId();
        return Result.success(contentService.getMyContent(userId, page, size));
    }

    @GetMapping("/my/drafts")
    public Result<PageResult<ContentVO>> myDrafts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = SecurityUtil.getCurrentUserId();
        return Result.success(contentService.getMyDrafts(userId, page, size));
    }

    @PostMapping("/{id}/like")
    public Result<Boolean> like(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        boolean nowLiked = contentService.toggleLike(id, userId);
        return Result.success(nowLiked);
    }
}
