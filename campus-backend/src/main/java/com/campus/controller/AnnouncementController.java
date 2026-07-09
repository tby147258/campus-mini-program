package com.campus.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.annotation.NoAuth;
import com.campus.annotation.RoleRequired;
import com.campus.common.Result;
import com.campus.entity.Announcement;
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
        q.eq(Announcement::getStatus, 1)
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
        announcement.setStatus(1);
        announcementService.save(announcement);
        return Result.success(null);
    }

    @PutMapping("/{id}")
    @RoleRequired(1)
    public Result<?> update(@PathVariable Long id, @RequestBody Announcement announcement) {
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