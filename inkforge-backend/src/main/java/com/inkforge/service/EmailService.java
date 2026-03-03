package com.inkforge.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.inkforge.common.exception.BusinessException;
import com.inkforge.entity.EmailCode;
import com.inkforge.mapper.EmailCodeMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmailCodeMapper emailCodeMapper;

    @Value("${spring.mail.username}")
    private String from;

    public EmailService(JavaMailSender mailSender, EmailCodeMapper emailCodeMapper) {
        this.mailSender = mailSender;
        this.emailCodeMapper = emailCodeMapper;
    }

    public void sendCode(String email, String purpose) {
        // 生成6位随机数字验证码
        String code = String.format("%06d", new Random().nextInt(1000000));

        // 删除该邮箱同用途的旧验证码
        emailCodeMapper.delete(new LambdaQueryWrapper<EmailCode>()
                .eq(EmailCode::getEmail, email)
                .eq(EmailCode::getPurpose, purpose));

        // 保存新验证码，5分钟后过期
        EmailCode emailCode = new EmailCode();
        emailCode.setEmail(email);
        emailCode.setCode(code);
        emailCode.setPurpose(purpose);
        emailCode.setExpireTime(LocalDateTime.now().plusMinutes(5));
        emailCodeMapper.insert(emailCode);

        // 发送邮件
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(email);
            message.setSubject("InkForge 邮箱验证码");
            message.setText("您好！\n\n您的 InkForge 邮箱验证码为：" + code + "\n\n验证码有效期为 5 分钟，请勿泄露给他人。\n\n如非本人操作，请忽略此邮件。\n\n— InkForge 团队");
            mailSender.send(message);
        } catch (Exception e) {
            // 发送失败时回滚验证码记录
            emailCodeMapper.delete(new LambdaQueryWrapper<EmailCode>()
                    .eq(EmailCode::getEmail, email)
                    .eq(EmailCode::getPurpose, purpose));
            throw new BusinessException("邮件发送失败，请检查邮箱地址是否正确");
        }
    }

    public boolean verifyCode(String email, String code, String purpose) {
        EmailCode emailCode = emailCodeMapper.selectOne(new LambdaQueryWrapper<EmailCode>()
                .eq(EmailCode::getEmail, email)
                .eq(EmailCode::getCode, code)
                .eq(EmailCode::getPurpose, purpose)
                .gt(EmailCode::getExpireTime, LocalDateTime.now())
                .last("LIMIT 1"));
        if (emailCode == null) return false;
        // 验证成功后删除，防止重复使用
        emailCodeMapper.deleteById(emailCode.getId());
        return true;
    }
}
