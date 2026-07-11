package com.campus.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.annotation.NoAuth;
import com.campus.annotation.RoleRequired;
import com.campus.common.Result;
import com.campus.common.UserContext;
import com.campus.entity.LostFound;
import com.campus.enums.LostFoundStatus;
import com.campus.enums.LostFoundType;
import com.campus.service.LostFoundService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lost-found")
@SuppressWarnings("null")
public class LostFoundController {
    private final LostFoundService lostFoundService;

    public LostFoundController(LostFoundService lostFoundService) {
        this.lostFoundService = lostFoundService;
    }

    @GetMapping
    @NoAuth
    public Result<?> list(@RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int size,
                          @RequestParam(required = false) Integer type,
                          @RequestParam(required = false) Integer status,
                          @RequestParam(required = false) String keyword,
                          @RequestParam(required = false) Long userId) {
        Page<LostFound> p = new Page<>(page, size);
        Integer role = UserContext.getRole();
        boolean isAdmin = role != null && role.intValue() >= 1;
        // 管理员：不传status则查看全部，传status则按状态过滤；普通用户：只能查看已发布
        LambdaQueryWrapper<LostFound> q = new LambdaQueryWrapper<>();
        q.eq(type != null, LostFound::getType, type != null ? LostFoundType.fromCode(type) : null);
        if (isAdmin) {
            if (status != null) {
                q.eq(LostFound::getStatus, LostFoundStatus.fromCode(status));
            }
        } else {
            q.eq(LostFound::getStatus, LostFoundStatus.PUBLISHED);
        }
        // 按发布人过滤（个人中心"我的发布"）
        q.eq(userId != null, LostFound::getUserId, userId);
        // D17: 关键字搜索（物品名称/地点/描述）
        if (keyword != null && !keyword.isBlank()) {
            q.and(w -> w
                    .like(LostFound::getItemName, keyword)
                    .or()
                    .like(LostFound::getLocation, keyword)
                    .or()
                    .like(LostFound::getDescription, keyword));
        }
        q.orderByDesc(LostFound::getCreatedAt);
        return Result.success(lostFoundService.page(p, q));
    }

    @GetMapping("/{id}")
    @NoAuth
    public Result<?> getById(@PathVariable Long id) {
        LostFound record = lostFoundService.getById(id);
        if (record == null) {
            return Result.error(404, "记录不存在");
        }
        // D10: 非管理员只能查看已发布或已结束的记录
        Integer role = UserContext.getRole();
        boolean isAdmin = role != null && role.intValue() >= 1;
        if (!isAdmin && record.getStatus() != LostFoundStatus.PUBLISHED && record.getStatus() != LostFoundStatus.CLOSED) {
            return Result.error(403, "无权查看此记录");
        }
        return Result.success(record);
    }

    @PostMapping
    public Result<?> create(@RequestBody LostFound lostFound) {
        Long userId = UserContext.getUserId();
        if (userId == null) return Result.error(401, "未登录");

        // D16: 手动校验必填字段（实体跨端点复用，不宜使用 @Valid）
        if (lostFound.getItemName() == null || lostFound.getItemName().isBlank()) {
            return Result.error(400, "物品名称不能为空");
        }
        if (lostFound.getDescription() == null || lostFound.getDescription().isBlank()) {
            return Result.error(400, "物品描述不能为空");
        }
        if (lostFound.getContactPerson() == null || lostFound.getContactPerson().isBlank()) {
            return Result.error(400, "联系人不能为空");
        }
        lostFoundService.createLostFound(lostFound, userId);
        return Result.success(null);
    }

    @PutMapping("/{id}")
    @RoleRequired(1)
    public Result<?> update(@PathVariable Long id, @RequestBody LostFound req) {
        try {
            Integer role = UserContext.getRole();
            Long currentUserId = UserContext.getUserId();
            boolean isAdmin = role != null && role.intValue() >= 1;
            boolean ok = lostFoundService.updateLostFound(id, req, currentUserId, isAdmin);
            if (!ok) {
                return Result.error(404, "记录不存在");
            }
            return Result.success(null);
        } catch (SecurityException e) {
            return Result.error(403, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @RoleRequired(1)
    public Result<?> delete(@PathVariable Long id) {
        lostFoundService.removeById(id);
        return Result.success(null);
    }

    @PutMapping("/{id}/audit")
    @RoleRequired(1)
    public Result<?> audit(@PathVariable Long id, @RequestBody LostFound req) {
        // D17: 校验 status 非空
        LostFoundStatus status = req.getStatus();
        if (status == null) {
            return Result.error(400, "审核状态不能为空");
        }
        Long auditorId = UserContext.getUserId();
        boolean ok = lostFoundService.auditLostFound(id, status, auditorId, req.getRejectReason());
        if (!ok) {
            return Result.error(404, "记录不存在");
        }
        return Result.success(null);
    }
}