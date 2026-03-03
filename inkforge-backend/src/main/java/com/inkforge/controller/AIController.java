package com.inkforge.controller;

import com.inkforge.common.Result;
import com.inkforge.dto.request.AiRequest;
import com.inkforge.service.ai.AIService;
import com.inkforge.util.SecurityUtil;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    private final AIService aiService;

    public AIController(AIService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/generate")
    public Result<String> generate(@Valid @RequestBody AiRequest req) {
        Long userId = SecurityUtil.getCurrentUserId();
        return Result.success(aiService.generate(req.getContent(), userId));
    }

    @PostMapping("/summary")
    public Result<String> summary(@Valid @RequestBody AiRequest req) {
        Long userId = SecurityUtil.getCurrentUserId();
        return Result.success(aiService.summary(req.getContent(), userId));
    }

    @PostMapping("/polish")
    public Result<String> polish(@Valid @RequestBody AiRequest req) {
        Long userId = SecurityUtil.getCurrentUserId();
        return Result.success(aiService.polish(req.getContent(), userId));
    }

    @PostMapping("/keywords")
    public Result<String> keywords(@Valid @RequestBody AiRequest req) {
        Long userId = SecurityUtil.getCurrentUserId();
        return Result.success(aiService.keywords(req.getContent(), userId));
    }

    @PostMapping("/sentiment")
    public Result<String> sentiment(@Valid @RequestBody AiRequest req) {
        Long userId = SecurityUtil.getCurrentUserId();
        return Result.success(aiService.sentiment(req.getContent(), userId));
    }

    @PostMapping("/style")
    public Result<String> style(@Valid @RequestBody AiRequest req) {
        Long userId = SecurityUtil.getCurrentUserId();
        return Result.success(aiService.style(req.getContent(), userId));
    }

    @PostMapping("/qa")
    public Result<String> qa(@Valid @RequestBody AiRequest req) {
        Long userId = SecurityUtil.getCurrentUserId();
        return Result.success(aiService.qa(req.getContent(), req.getQuestion(), userId));
    }
}
