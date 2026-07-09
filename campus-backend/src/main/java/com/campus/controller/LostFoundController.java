package com.campus.controller;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
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

import java.time.LocalDateTime;

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
                          @RequestParam(required = false) Integer type) {
        Page<LostFound> p = new Page<>(page, size);
        return Result.success(lostFoundService.lambdaQuery()
                .eq(type != null, LostFound::getType, type != null ? LostFoundType.fromCode(type) : null)
                .eq(LostFound::getStatus, LostFoundStatus.PUBLISHED)
                .orderByDesc(LostFound::getCreatedAt)
                .page(p));
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
        lostFound.setUserId(userId);
        lostFound.setStatus(LostFoundStatus.PENDING_AUDIT); // 待审核
        lostFoundService.save(lostFound);
        return Result.success(null);
    }

    @PutMapping("/{id}")
    @RoleRequired(1)
    public Result<?> update(@PathVariable Long id, @RequestBody LostFound req) {
        // D14: 校验记录是否存在
        LostFound exist = lostFoundService.getById(id);
        if (exist == null) {
            return Result.error(404, "记录不存在");
        }
        // D3: LambdaUpdateWrapper 白名单方式，只更新允许的字段
        LambdaUpdateWrapper<LostFound> wrapper = new LambdaUpdateWrapper<LostFound>()
                .eq(LostFound::getId, id)
                .set(req.getItemName() != null, LostFound::getItemName, req.getItemName())
                .set(req.getCategory() != null, LostFound::getCategory, req.getCategory())
                .set(req.getDescription() != null, LostFound::getDescription, req.getDescription())
                .set(req.getLocation() != null, LostFound::getLocation, req.getLocation())
                .set(req.getContactPerson() != null, LostFound::getContactPerson, req.getContactPerson())
                .set(req.getContactPhone() != null, LostFound::getContactPhone, req.getContactPhone())
                .set(req.getImages() != null, LostFound::getImages, req.getImages());
        lostFoundService.update(wrapper);
        return Result.success(null);
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
        LostFound exist = lostFoundService.getById(id);
        if (exist == null) {
            return Result.error(404, "记录不存在");
        }
        // D4: 补全审核记录
        LambdaUpdateWrapper<LostFound> wrapper = new LambdaUpdateWrapper<LostFound>()
                .eq(LostFound::getId, id)
                .set(LostFound::getStatus, req.getStatus())
                .set(LostFound::getAuditorId, UserContext.getUserId())
                .set(LostFound::getAuditTime, LocalDateTime.now());
        // 驳回时记录驳回原因
        if (req.getStatus() == LostFoundStatus.REJECTED && req.getRejectReason() != null) {
            wrapper.set(LostFound::getRejectReason, req.getRejectReason());
        }
        lostFoundService.update(wrapper);
        return Result.success(null);
    }
}