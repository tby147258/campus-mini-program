package com.campus.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.annotation.RoleRequired;
import com.campus.common.Result;
import com.campus.common.UserContext;
import com.campus.entity.RepairOrder;
import com.campus.enums.RepairOrderStatus;
import com.campus.service.RepairOrderService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/repair")
public class RepairOrderController {
    private final RepairOrderService repairOrderService;

    public RepairOrderController(RepairOrderService repairOrderService) {
        this.repairOrderService = repairOrderService;
    }

    @GetMapping
    @RoleRequired(1)
    public Result<?> list(@RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int size,
                          @RequestParam(required = false) Integer status) {
        Page<RepairOrder> p = new Page<>(page, size);
        LambdaQueryWrapper<RepairOrder> q = new LambdaQueryWrapper<>();
        q.eq(status != null, RepairOrder::getStatus, status != null ? RepairOrderStatus.fromCode(status) : null)
         .orderByDesc(RepairOrder::getCreatedAt);
        return Result.success(repairOrderService.page(p, q));
    }

    @GetMapping("/my-orders")
    public Result<?> myOrders(@RequestParam(defaultValue = "1") int page,
                              @RequestParam(defaultValue = "10") int size) {
        Long userId = UserContext.getUserId();
        if (userId == null) return Result.error(401, "未登录");
        Page<RepairOrder> p = new Page<>(page, size);
        return Result.success(repairOrderService.lambdaQuery()
                .eq(RepairOrder::getUserId, userId)
                .orderByDesc(RepairOrder::getCreatedAt)
                .page(p));
    }

    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Long id) {
        RepairOrder order = repairOrderService.getById(id);
        if (order == null) {
            return Result.error(404, "工单不存在");
        }
        // 验证权限：管理员可看所有，学生只能看自己的工单
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null) {
            return Result.error(401, "未登录");
        }
        if (!currentUserId.equals(order.getUserId())) {
            Integer role = UserContext.getRole();
            if (role == null || role.intValue() < 1) {
                return Result.error(403, "无权查看此工单");
            }
        }
        return Result.success(order);
    }

    @PostMapping
    public Result<?> create(@RequestBody RepairOrder repairOrder) {
        Long userId = UserContext.getUserId();
        if (userId == null) return Result.error(401, "未登录");
        // RS6: 必填字段校验
        if (repairOrder.getRepairType() == null || repairOrder.getRepairType().isBlank()) {
            return Result.error(400, "报修类型不能为空");
        }
        if (repairOrder.getDescription() == null || repairOrder.getDescription().isBlank()) {
            return Result.error(400, "报修描述不能为空");
        }
        repairOrderService.createOrder(repairOrder, userId);
        return Result.success(null);
    }

    @PutMapping("/{id}/status")
    @RoleRequired(1)
    public Result<?> updateStatus(@PathVariable Long id, @RequestBody RepairOrder update) {
        // RS3: 非空校验移入 Service，Controller 仍做防御
        if (update.getStatus() == null) {
            return Result.error(400, "工单状态不能为空");
        }
        Long handlerId = UserContext.getUserId();
        boolean ok = repairOrderService.updateStatus(id, update.getStatus(), handlerId,
                update.getRejectReason(), update.getHandleResult());
        if (!ok) {
            return Result.error(404, "工单不存在");
        }
        return Result.success(null);
    }
}