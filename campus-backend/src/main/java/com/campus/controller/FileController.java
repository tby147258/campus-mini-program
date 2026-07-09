package com.campus.controller;

import com.campus.annotation.NoAuth;
import com.campus.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.UUID;

@RestController
@RequestMapping("/api/file")
public class FileController {
    @Value("${file.upload-path}")
    private String uploadPath;

    @PostMapping("/upload")
    @NoAuth
    public Result<?> upload(@RequestParam("file") MultipartFile file) {
        try {
            String originalName = file.getOriginalFilename();
            if (originalName == null || originalName.isBlank()) {
                return Result.error(400, "文件名为空");
            }
            String ext = originalName.substring(originalName.lastIndexOf("."));
            String fileName = UUID.randomUUID() + ext;
            File dest = new File(uploadPath, fileName);
            if (!dest.getParentFile().exists()) dest.getParentFile().mkdirs();
            file.transferTo(dest);
            return Result.success("/uploads/" + fileName);
        } catch (Exception e) {
            return Result.error(500, "上传失败: " + e.getMessage());
        }
    }
}