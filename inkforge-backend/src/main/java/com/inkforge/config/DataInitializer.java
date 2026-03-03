package com.inkforge.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.inkforge.entity.User;
import com.inkforge.mapper.UserMapper;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 应用启动时初始化数据：确保管理员账号存在
 */
@Component
public class DataInitializer implements ApplicationRunner {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        User admin = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, "admin"));
        if (admin == null) {
            // 不存在则创建
            admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole("admin");
            admin.setStatus("正常");
            admin.setCreateTime(LocalDateTime.now());
            userMapper.insert(admin);
            System.out.println("[InkForge] 管理员账号已创建 - 用户名: admin, 密码: admin123");
        } else if (!"admin".equals(admin.getRole())) {
            // 存在但角色不对，修正
            admin.setRole("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            userMapper.updateById(admin);
            System.out.println("[InkForge] 管理员账号已修正");
        } else {
            // 存在且角色正确，重置密码确保可以登录
            admin.setPassword(passwordEncoder.encode("admin123"));
            userMapper.updateById(admin);
            System.out.println("[InkForge] 管理员密码已重置 - 用户名: admin, 密码: admin123");
        }
    }
}
