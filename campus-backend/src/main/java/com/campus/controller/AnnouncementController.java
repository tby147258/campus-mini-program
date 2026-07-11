package com.campus.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.annotation.NoAuth;
import com.campus.annotation.RoleRequired;
import com.campus.common.Result;
import com.campus.common.UserContext;
import com.campus.entity.Announcement;
import com.campus.enums.AnnouncementStatus;
import com.campus.service.AnnouncementService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/announcement")
@SuppressWarnings("null")
public class AnnouncementController {
    private final AnnouncementService announcementService;

    public AnnouncementController(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    @GetMapping
    @NoAuth
    public Result<?> list(@RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int size,
                          @RequestParam(required = false) String category) {
        Page<Announcement> p = new Page<>(page, size);
        LambdaQueryWrapper<Announcement> q = new LambdaQueryWrapper<>();
        q.eq(Announcement::getStatus, AnnouncementStatus.PUBLISHED)
         .eq(category != null && !category.isBlank(), Announcement::getCategory, category)
         .orderByDesc(Announcement::getCreatedAt);
        return Result.success(announcementService.page(p, q));
    }

    @GetMapping("/{id}")
    @NoAuth
    public Result<?> getById(@PathVariable Long id) {
        return Result.success(announcementService.getById(id));
    }

    @PostMapping
    @RoleRequired(1)
    public Result<?> create(@RequestBody Announcement announcement) {
        // 必填字段校验
        if (announcement.getTitle() == null || announcement.getTitle().isBlank()) {
            return Result.error(400, "公告标题不能为空");
        }
        if (announcement.getContent() == null || announcement.getContent().isBlank()) {
            return Result.error(400, "公告内容不能为空");
        }
        // 从当前登录上下文获取发布人
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        announcement.setPublisherId(userId);
        announcement.setStatus(AnnouncementStatus.PUBLISHED);
        announcementService.save(announcement);
        return Result.success(null);
    }

    @PutMapping("/{id}")
    @RoleRequired(1)
    public Result<?> update(@PathVariable Long id, @RequestBody Announcement announcement) {
        // 必填字段校验
        if (announcement.getTitle() == null || announcement.getTitle().isBlank()) {
            return Result.error(400, "公告标题不能为空");
        }
        if (announcement.getContent() == null || announcement.getContent().isBlank()) {
            return Result.error(400, "公告内容不能为空");
        }
        announcement.setId(id);
        announcementService.updateById(announcement);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    @RoleRequired(1)
    public Result<?> delete(@PathVariable Long id) {
        announcementService.removeById(id);
        return Result.success(null);
    }
}