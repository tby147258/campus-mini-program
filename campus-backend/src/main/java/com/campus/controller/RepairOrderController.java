package com.campus.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.Result;
import com.campus.entity.RepairOrder;
import com.campus.service.RepairOrderService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/repair-orders")
public class RepairOrderController {
    private final RepairOrderService repairOrderService;

    public RepairOrderController(RepairOrderService repairOrderService) {
        this.repairOrderService = repairOrderService;
    }

    @GetMapping
    public Result<?> list(@RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int size,
                          @RequestParam(required = false) Integer status) {
        Page<RepairOrder> p = new Page<>(page, size);
        LambdaQueryWrapper<RepairOrder> q = new LambdaQueryWrapper<>();
        q.eq(status != null, RepairOrder::getStatus, status)
         .orderByDesc(RepairOrder::getCreatedAt);
        return Result.success(repairOrderService.page(p, q));
    }

    @GetMapping("/my")
    public Result<?> myOrders(@RequestParam Long userId,
                              @RequestParam(defaultValue = "1") int page,
                              @RequestParam(defaultValue = "10") int size) {
        Page<RepairOrder> p = new Page<>(page, size);
        return Result.success(repairOrderService.lambdaQuery()
                .eq(RepairOrder::getUserId, userId)
                .orderByDesc(RepairOrder::getCreatedAt)
                .page(p));
    }

    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Long id) {
        return Result.success(repairOrderService.getById(id));
    }

    @PostMapping
    public Result<?> create(@RequestBody RepairOrder repairOrder) {
        repairOrder.setStatus(0); // 待处理
        repairOrderService.save(repairOrder);
        return Result.success(null);
    }

    @PutMapping("/{id}/status")
    public Result<?> updateStatus(@PathVariable Long id, @RequestBody RepairOrder update) {
        RepairOrder order = repairOrderService.getById(id);
        if (order == null) return Result.error(404, "工单不存在");
        order.setStatus(update.getStatus());
        if (update.getStatus() == 1) order.setHandleTime(LocalDateTime.now());
        if (update.getStatus() == 2) order.setCompleteTime(LocalDateTime.now());
        order.setRejectReason(update.getRejectReason());
        order.setHandleResult(update.getHandleResult());
        repairOrderService.updateById(order);
        return Result.success(null);
    }
}