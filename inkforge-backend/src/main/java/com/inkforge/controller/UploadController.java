package com.inkforge.controller;

import com.inkforge.common.Result;
import com.inkforge.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    @Value("${app.upload.path}")
    private String uploadPath;

    @Value("${app.upload.base-url}")
    private String baseUrl;

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );

    @PostMapping("/image")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException(400, "文件不能为空");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
            throw new BusinessException(400, "只支持 JPG、PNG、GIF、WebP 格式");
        }
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new BusinessException(400, "图片大小不能超过 10MB");
        }

        // 使用绝对路径，避免嵌入式 Tomcat 相对路径解析到临时目录的问题
        Path uploadDir = Paths.get(uploadPath).toAbsolutePath().normalize();
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            throw new BusinessException(500, "创建上传目录失败：" + e.getMessage());
        }

        // 生成唯一文件名（保留原扩展名）
        String original = file.getOriginalFilename();
        String ext = (original != null && original.contains("."))
                ? original.substring(original.lastIndexOf('.')).toLowerCase()
                : ".jpg";
        String filename = UUID.randomUUID().toString().replace("-", "") + ext;

        Path target = uploadDir.resolve(filename);
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new BusinessException(500, "文件保存失败：" + e.getMessage());
        }

        String url = baseUrl.endsWith("/") ? baseUrl + filename : baseUrl + "/" + filename;
        return Result.success(url);
    }
}
