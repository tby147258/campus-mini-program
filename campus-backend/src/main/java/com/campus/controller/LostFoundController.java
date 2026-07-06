package com.campus.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.annotation.RoleRequired;
import com.campus.common.Result;
import com.campus.common.UserContext;
import com.campus.entity.LostFound;
import com.campus.service.LostFoundService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lost-found")
public class LostFoundController {
    private final LostFoundService lostFoundService;

    public LostFoundController(LostFoundService lostFoundService) {
        this.lostFoundService = lostFoundService;
    }

    @GetMapping
    public Result<?> list(@RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int size,
                          @RequestParam(required = false) Integer type) {
        Page<LostFound> p = new Page<>(page, size);
        return Result.success(lostFoundService.lambdaQuery()
                .eq(type != null, LostFound::getType, type)
                .eq(LostFound::getStatus, 1)
                .orderByDesc(LostFound::getCreatedAt)
                .page(p));
    }

    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Long id) {
        return Result.success(lostFoundService.getById(id));
    }

    @PostMapping
    public Result<?> create(@RequestBody LostFound lostFound) {
        Long userId = UserContext.getUserId();
        if (userId == null) return Result.error(401, "未登录");
        lostFound.setUserId(userId);
        lostFound.setStatus(0); // 待审核
        lostFoundService.save(lostFound);
        return Result.success(null);
    }

    @PutMapping("/{id}")
    @RoleRequired(1)
    public Result<?> update(@PathVariable Long id, @RequestBody LostFound lostFound) {
        lostFound.setId(id);
        lostFoundService.updateById(lostFound);
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
    public Result<?> audit(@PathVariable Long id, @RequestBody LostFound lostFound) {
        lostFoundService.lambdaUpdate()
                .eq(LostFound::getId, id)
                .set(LostFound::getStatus, lostFound.getStatus())
                .update();
        return Result.success(null);
    }
}