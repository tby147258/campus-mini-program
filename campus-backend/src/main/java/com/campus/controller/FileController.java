package com.campus.controller;

import com.campus.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/file")
public class FileController {

    private static final Logger log = LoggerFactory.getLogger(FileController.class);

    /** 允许的文件扩展名白名单 */
    private static final Set<String> ALLOWED_EXT = Set.of(
            ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp",   // 图片
            ".pdf", ".doc", ".docx", ".xls", ".xlsx",           // 文档
            ".txt", ".csv"                                       // 文本
    );

    /** 最大上传大小 10MB */
    private static final long MAX_SIZE = 10 * 1024 * 1024;

    @Value("${file.upload-path}")
    private String uploadPath;

    @PostMapping("/upload")
    public Result<?> upload(@RequestParam("file") MultipartFile file) {
        // 空文件检查
        if (file.isEmpty() || file.getSize() == 0) {
            return Result.error(400, "文件不能为空");
        }

        // 大小限制
        if (file.getSize() > MAX_SIZE) {
            return Result.error(400, "文件大小不能超过10MB");
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            return Result.error(400, "文件名为空");
        }

        // 类型白名单校验
        String ext = originalName.substring(originalName.lastIndexOf(".")).toLowerCase();
        if (!ALLOWED_EXT.contains(ext)) {
            return Result.error(400, "不支持的文件类型: " + ext);
        }

        String fileName = UUID.randomUUID() + ext;
        try {
            File dest = new File(uploadPath, fileName);
            if (!dest.getParentFile().exists()) {
                dest.getParentFile().mkdirs();
            }
            file.transferTo(dest);
            log.info("文件上传成功: {} → {}", originalName, fileName);
            return Result.success("/uploads/" + fileName);
        } catch (Exception e) {
            log.error("文件上传失败: {}", originalName, e);
            return Result.error(500, "上传失败，请稍后重试");
        }
    }
}