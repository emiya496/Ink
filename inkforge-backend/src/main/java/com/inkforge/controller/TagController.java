package com.inkforge.controller;

import com.inkforge.common.Result;
import com.inkforge.dto.response.TagVO;
import com.inkforge.entity.Tag;
import com.inkforge.service.TagService;
import com.inkforge.util.SecurityUtil;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tag")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping("/list")
    public Result<List<Tag>> list(@RequestParam(required = false) String type) {
        return Result.success(tagService.list(type));
    }

    @PostMapping("/create")
    public Result<Tag> create(@RequestBody Map<String, String> body) {
        Long userId = SecurityUtil.getCurrentUserId();
        return Result.success(tagService.createCustomTag(body.get("tagName"), userId));
    }

    @GetMapping("/hot")
    public Result<List<TagVO>> hot(@RequestParam(defaultValue = "20") Integer limit) {
        return Result.success(tagService.getHotTags(limit));
    }
}
