package com.inkforge.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.inkforge.common.exception.BusinessException;
import com.inkforge.dto.request.LoginRequest;
import com.inkforge.dto.request.RegisterRequest;
import com.inkforge.dto.response.LoginResponse;
import com.inkforge.entity.User;
import com.inkforge.mapper.UserMapper;
import com.inkforge.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    public UserService(UserMapper userMapper, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, EmailService emailService) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.emailService = emailService;
    }

    public void register(RegisterRequest req) {
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, req.getUsername()));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }
        // 邮箱唯一性校验（注册时选填）
        if (req.getEmail() != null && !req.getEmail().isBlank()) {
            Long emailCount = userMapper.selectCount(
                    new LambdaQueryWrapper<User>().eq(User::getEmail, req.getEmail()));
            if (emailCount > 0) {
                throw new BusinessException("该邮箱已被其他账号绑定");
            }
        }
        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRole("user");
        user.setStatus("正常");
        if (req.getEmail() != null && !req.getEmail().isBlank()) {
            user.setEmail(req.getEmail());
        }
        userMapper.insert(user);
    }

    public LoginResponse login(LoginRequest req) {
        User user;
        // 支持邮箱登录：输入包含 @ 时按邮箱查询
        if (req.getUsername().contains("@")) {
            user = userMapper.selectOne(
                    new LambdaQueryWrapper<User>().eq(User::getEmail, req.getUsername()));
            if (user == null) {
                throw new BusinessException("该邮箱未注册或密码错误");
            }
        } else {
            user = userMapper.selectOne(
                    new LambdaQueryWrapper<User>().eq(User::getUsername, req.getUsername()));
            if (user == null) {
                throw new BusinessException("用户名或密码错误");
            }
        }
        if ("禁用".equals(user.getStatus())) {
            throw new BusinessException(403, "账号已被禁用，请联系管理员");
        }
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new BusinessException(req.getUsername().contains("@") ? "该邮箱未注册或密码错误" : "用户名或密码错误");
        }
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        LoginResponse resp = new LoginResponse();
        resp.setToken(token);
        resp.setUserId(user.getId());
        resp.setUsername(user.getUsername());
        resp.setAvatar(user.getAvatar());
        resp.setRole(user.getRole());
        return resp;
    }

    public User getById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return user;
    }

    public void updateInfo(Long userId, String avatar) {
        User user = new User();
        user.setId(userId);
        user.setAvatar(avatar);
        userMapper.updateById(user);
    }

    public void sendEmailCode(String email, String purpose, Long userId) {
        // 检查邮箱是否已被其他账号使用
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, email)
                .ne(User::getId, userId));
        if (count > 0) {
            throw new BusinessException("该邮箱已被其他账号绑定");
        }
        emailService.sendCode(email, purpose);
    }

    public void bindEmail(Long userId, String email, String code) {
        if (!emailService.verifyCode(email, code, "bind")) {
            throw new BusinessException("验证码错误或已过期");
        }
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, email)
                .ne(User::getId, userId));
        if (count > 0) {
            throw new BusinessException("该邮箱已被其他账号绑定");
        }
        User user = new User();
        user.setId(userId);
        user.setEmail(email);
        userMapper.updateById(user);
    }

    public void changeEmail(Long userId, String newEmail, String code) {
        if (!emailService.verifyCode(newEmail, code, "change")) {
            throw new BusinessException("验证码错误或已过期");
        }
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, newEmail)
                .ne(User::getId, userId));
        if (count > 0) {
            throw new BusinessException("该邮箱已被其他账号绑定");
        }
        User user = new User();
        user.setId(userId);
        user.setEmail(newEmail);
        userMapper.updateById(user);
    }

    // 找回密码：发送重置验证码（无需登录，公开接口）
    public void sendPasswordResetCode(String email) {
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getEmail, email));
        if (count == 0) {
            throw new BusinessException("该邮箱未绑定任何账号");
        }
        emailService.sendCode(email, "reset");
    }

    // 找回密码：验证码校验通过后重置密码
    public void resetPassword(String email, String code, String newPassword) {
        if (!emailService.verifyCode(email, code, "reset")) {
            throw new BusinessException("验证码错误或已过期");
        }
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getEmail, email));
        if (user == null) {
            throw new BusinessException("该邮箱未绑定任何账号");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }

    // 修改用户名
    public void updateUsername(Long userId, String newUsername) {
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, newUsername)
                .ne(User::getId, userId));
        if (count > 0) {
            throw new BusinessException("用户名已被占用");
        }
        User user = new User();
        user.setId(userId);
        user.setUsername(newUsername);
        userMapper.updateById(user);
    }

    // 修改密码（需验证旧密码）
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("当前密码错误");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }

    // 发送注销账户验证码
    public void sendDeleteAccountCode(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException(404, "用户不存在");
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new BusinessException("账户未绑定邮箱，请先绑定邮箱后再注销");
        }
        emailService.sendCode(user.getEmail(), "delete");
    }

    // 注销账户：有邮箱则需验证码，无邮箱仅验证密码
    public void deleteAccount(Long userId, String password, String code) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException(404, "用户不存在");
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException("密码错误");
        }
        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            if (code == null || code.isBlank()) {
                throw new BusinessException("请填写邮箱验证码");
            }
            if (!emailService.verifyCode(user.getEmail(), code, "delete")) {
                throw new BusinessException("验证码错误或已过期");
            }
        }
        userMapper.deleteById(userId);
    }
}
