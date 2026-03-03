package com.inkforge.controller;

import com.inkforge.common.Result;
import com.inkforge.dto.request.LoginRequest;
import com.inkforge.dto.request.RegisterRequest;
import com.inkforge.dto.response.LoginResponse;
import com.inkforge.entity.User;
import com.inkforge.service.UserService;
import com.inkforge.util.SecurityUtil;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest req) {
        userService.register(req);
        return Result.success();
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        return Result.success(userService.login(req));
    }

    @GetMapping("/info")
    public Result<User> info() {
        Long userId = SecurityUtil.getCurrentUserId();
        return Result.success(userService.getById(userId));
    }

    @PutMapping("/info")
    public Result<Void> updateInfo(@RequestBody Map<String, String> body) {
        Long userId = SecurityUtil.getCurrentUserId();
        userService.updateInfo(userId, body.get("avatar"));
        return Result.success();
    }

    @PostMapping("/email/send-code")
    public Result<Void> sendEmailCode(@RequestBody Map<String, String> body) {
        Long userId = SecurityUtil.getCurrentUserId();
        userService.sendEmailCode(body.get("email"), body.get("purpose"), userId);
        return Result.success();
    }

    @PostMapping("/email/bind")
    public Result<Void> bindEmail(@RequestBody Map<String, String> body) {
        Long userId = SecurityUtil.getCurrentUserId();
        userService.bindEmail(userId, body.get("email"), body.get("code"));
        return Result.success();
    }

    @PutMapping("/email/change")
    public Result<Void> changeEmail(@RequestBody Map<String, String> body) {
        Long userId = SecurityUtil.getCurrentUserId();
        userService.changeEmail(userId, body.get("email"), body.get("code"));
        return Result.success();
    }

    // 找回密码（无需登录）
    @PostMapping("/password/send-code")
    public Result<Void> sendPasswordResetCode(@RequestBody Map<String, String> body) {
        userService.sendPasswordResetCode(body.get("email"));
        return Result.success();
    }

    @PostMapping("/password/reset")
    public Result<Void> resetPassword(@RequestBody Map<String, String> body) {
        userService.resetPassword(body.get("email"), body.get("code"), body.get("newPassword"));
        return Result.success();
    }

    @PutMapping("/username")
    public Result<Void> updateUsername(@RequestBody Map<String, String> body) {
        Long userId = SecurityUtil.getCurrentUserId();
        userService.updateUsername(userId, body.get("newUsername"));
        return Result.success();
    }

    @PutMapping("/password")
    public Result<Void> changePassword(@RequestBody Map<String, String> body) {
        Long userId = SecurityUtil.getCurrentUserId();
        userService.changePassword(userId, body.get("oldPassword"), body.get("newPassword"));
        return Result.success();
    }

    @PostMapping("/account/send-delete-code")
    public Result<Void> sendDeleteCode() {
        Long userId = SecurityUtil.getCurrentUserId();
        userService.sendDeleteAccountCode(userId);
        return Result.success();
    }

    @DeleteMapping("/account")
    public Result<Void> deleteAccount(@RequestBody Map<String, String> body) {
        Long userId = SecurityUtil.getCurrentUserId();
        userService.deleteAccount(userId, body.get("password"), body.get("code"));
        return Result.success();
    }
}
