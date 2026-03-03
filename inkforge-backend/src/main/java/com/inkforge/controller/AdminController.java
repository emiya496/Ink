package com.inkforge.controller;

import com.inkforge.common.PageResult;
import com.inkforge.common.Result;
import com.inkforge.entity.*;
import com.inkforge.service.AdminService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // ========== 用户管理 ==========
    @GetMapping("/users")
    public Result<PageResult<User>> listUsers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        return Result.success(adminService.listUsers(page, size, keyword));
    }

    @PutMapping("/users/{id}/status")
    public Result<Void> updateUserStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        adminService.updateUserStatus(id, body.get("status"));
        return Result.success();
    }

    @DeleteMapping("/users/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return Result.success();
    }

    // ========== 内容管理 ==========
    @GetMapping("/contents")
    public Result<PageResult<Content>> listContents(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status) {
        return Result.success(adminService.listContents(page, size, type, status));
    }

    @PutMapping("/contents/{id}/status")
    public Result<Void> updateContentStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        adminService.updateContentStatus(id, body.get("status"));
        return Result.success();
    }

    @DeleteMapping("/contents/{id}")
    public Result<Void> deleteContent(@PathVariable Long id) {
        adminService.deleteContent(id);
        return Result.success();
    }

    // ========== 标签管理 ==========
    @GetMapping("/tags")
    public Result<PageResult<Tag>> listTags(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        return Result.success(adminService.listTags(page, size));
    }

    @PostMapping("/tags")
    public Result<Void> createTag(@RequestBody Map<String, String> body) {
        adminService.createSystemTag(body.get("tagName"));
        return Result.success();
    }

    @PutMapping("/tags/{id}/status")
    public Result<Void> updateTagStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        adminService.updateTagStatus(id, body.get("status"));
        return Result.success();
    }

    @DeleteMapping("/tags/{id}")
    public Result<Void> deleteTag(@PathVariable Long id) {
        adminService.deleteTag(id);
        return Result.success();
    }

    // ========== 评论管理 ==========
    @GetMapping("/comments")
    public Result<PageResult<Comment>> listComments(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        return Result.success(adminService.listComments(page, size));
    }

    @DeleteMapping("/comments/{id}")
    public Result<Void> deleteComment(@PathVariable Long id) {
        adminService.deleteComment(id);
        return Result.success();
    }

    // ========== AI统计 ==========
    @GetMapping("/ai/stats")
    public Result<Map<String, Object>> aiStats() {
        return Result.success(adminService.getAiStats());
    }
}
