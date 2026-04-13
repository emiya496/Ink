package com.inkforge.controller;

import com.inkforge.common.PageResult;
import com.inkforge.common.Result;
import com.inkforge.dto.response.UserProfileVO;
import com.inkforge.service.FollowService;
import com.inkforge.util.SecurityUtil;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/follow")
public class FollowController {

    private final FollowService followService;

    public FollowController(FollowService followService) {
        this.followService = followService;
    }

    @PostMapping("/{userId}")
    public Result<Void> follow(@PathVariable Long userId) {
        Long followerId = SecurityUtil.getCurrentUserId();
        followService.follow(followerId, userId);
        return Result.success();
    }

    @DeleteMapping("/{userId}")
    public Result<Void> unfollow(@PathVariable Long userId) {
        Long followerId = SecurityUtil.getCurrentUserId();
        followService.unfollow(followerId, userId);
        return Result.success();
    }

    @DeleteMapping("/follower/{followerId}")
    public Result<Void> removeFollower(@PathVariable Long followerId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        followService.removeFollower(currentUserId, followerId);
        return Result.success();
    }

    @GetMapping("/check/{userId}")
    public Result<Boolean> check(@PathVariable Long userId) {
        Long followerId = SecurityUtil.getCurrentUserId();
        return Result.success(followService.isFollowing(followerId, userId));
    }

    @GetMapping("/{userId}/followers")
    public Result<PageResult<UserProfileVO>> getFollowers(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(followService.getFollowers(userId, page, size));
    }

    @GetMapping("/{userId}/following")
    public Result<PageResult<UserProfileVO>> getFollowing(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(followService.getFollowing(userId, page, size));
    }
}
