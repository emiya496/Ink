package com.inkforge.controller;

import com.inkforge.common.PageResult;
import com.inkforge.common.Result;
import com.inkforge.dto.response.ContentVO;
import com.inkforge.service.FavoriteService;
import com.inkforge.util.SecurityUtil;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/favorite")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @PostMapping("/{contentId}")
    public Result<Void> add(@PathVariable Long contentId) {
        Long userId = SecurityUtil.getCurrentUserId();
        favoriteService.addFavorite(userId, contentId);
        return Result.success();
    }

    @DeleteMapping("/{contentId}")
    public Result<Void> remove(@PathVariable Long contentId) {
        Long userId = SecurityUtil.getCurrentUserId();
        favoriteService.removeFavorite(userId, contentId);
        return Result.success();
    }

    @GetMapping("/list")
    public Result<PageResult<ContentVO>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = SecurityUtil.getCurrentUserId();
        return Result.success(favoriteService.listFavorites(userId, page, size));
    }

    @GetMapping("/check/{contentId}")
    public Result<Boolean> check(@PathVariable Long contentId) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) return Result.success(false);
        return Result.success(favoriteService.isFavorited(userId, contentId));
    }
}
