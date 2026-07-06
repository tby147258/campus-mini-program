package com.campus.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.annotation.RoleRequired;
import com.campus.common.Result;
import com.campus.entity.OperationLog;
import com.campus.service.OperationLogService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/operation-logs")
@RoleRequired(1)
public class OperationLogController {

    private final OperationLogService operationLogService;

    public OperationLogController(OperationLogService operationLogService) {
        this.operationLogService = operationLogService;
    }

    /**
     * 分页查询操作日志
     */
    @GetMapping
    public Result<?> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        Page<OperationLog> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();

        // 按模块筛选
        if (module != null && !module.isBlank()) {
            wrapper.eq(OperationLog::getModule, module);
        }
        // 按操作类型筛选
        if (action != null && !action.isBlank()) {
            wrapper.eq(OperationLog::getAction, action);
        }
        // 按日期范围筛选
        if (startDate != null && !startDate.isBlank()) {
            wrapper.ge(OperationLog::getCreatedAt, startDate + " 00:00:00");
        }
        if (endDate != null && !endDate.isBlank()) {
            wrapper.le(OperationLog::getCreatedAt, endDate + " 23:59:59");
        }

        // 按时间倒序
        wrapper.orderByDesc(OperationLog::getCreatedAt);

        return Result.success(operationLogService.page(pageObj, wrapper));
    }
}