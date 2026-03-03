package com.inkforge.controller;

import com.inkforge.common.Result;
import com.inkforge.entity.Chapter;
import com.inkforge.service.ChapterService;
import com.inkforge.util.SecurityUtil;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chapter")
public class ChapterController {

    private final ChapterService chapterService;

    public ChapterController(ChapterService chapterService) {
        this.chapterService = chapterService;
    }

    @PostMapping
    public Result<Void> add(@RequestBody Map<String, Object> body) {
        Long contentId = Long.valueOf(body.get("contentId").toString());
        String title = (String) body.get("chapterTitle");
        String content = (String) body.get("chapterContent");
        Long userId = SecurityUtil.getCurrentUserId();
        chapterService.addChapter(contentId, title, content, userId);
        return Result.success();
    }

    @GetMapping("/{contentId}")
    public Result<List<Chapter>> list(@PathVariable Long contentId) {
        return Result.success(chapterService.listByContentId(contentId));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String title = (String) body.get("chapterTitle");
        String content = (String) body.get("chapterContent");
        Long userId = SecurityUtil.getCurrentUserId();
        chapterService.updateChapter(id, title, content, userId);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        chapterService.deleteChapter(id, userId);
        return Result.success();
    }
}
