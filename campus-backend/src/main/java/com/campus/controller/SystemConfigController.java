package com.campus.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.annotation.RoleRequired;
import com.campus.common.Result;
import com.campus.common.UserContext;
import com.campus.entity.OperationLog;
import com.campus.entity.SystemConfig;
import com.campus.service.OperationLogService;
import com.campus.service.SystemConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/system-config")
@SuppressWarnings("null")
public class SystemConfigController {

    private static final Logger log = LoggerFactory.getLogger(SystemConfigController.class);

    private final SystemConfigService systemConfigService;
    private final OperationLogService operationLogService;

    public SystemConfigController(SystemConfigService systemConfigService,
                                  OperationLogService operationLogService) {
        this.systemConfigService = systemConfigService;
        this.operationLogService = operationLogService;
    }

    @GetMapping
    @RoleRequired(1)
    public Result<?> list() {
        LambdaQueryWrapper<SystemConfig> q = new LambdaQueryWrapper<>();
        q.orderByAsc(SystemConfig::getId);
        return Result.success(systemConfigService.list(q));
    }

    @GetMapping("/{key}")
    @RoleRequired(1)
    public Result<?> getByKey(@PathVariable String key) {
        SystemConfig config = systemConfigService.getByKey(key);
        if (config == null) {
            return Result.error(404, "配置不存在");
        }
        return Result.success(config);
    }

    @PutMapping("/{key}")
    @RoleRequired(1)
    public Result<?> update(@PathVariable String key, @RequestBody Map<String, String> body) {
        SystemConfig config = systemConfigService.getByKey(key);
        if (config == null) {
            return Result.error(404, "配置不存在");
        }
        config.setConfigValue(body.get("configValue"));
        systemConfigService.updateById(config);

        saveLog("update", config.getId(), "修改系统配置: " + key);
        return Result.success(null);
    }

    @PutMapping("/batch")
    @RoleRequired(1)
    public Result<?> batchUpdate(@RequestBody List<SystemConfig> configs) {
        systemConfigService.updateBatchById(configs);

        saveLog("batch_update", null, "批量更新系统配置，数量: " + configs.size());
        return Result.success(null);
    }

    @PostMapping
    @RoleRequired(1)
    public Result<?> create(@RequestBody SystemConfig config) {
        SystemConfig existing = systemConfigService.getByKey(config.getConfigKey());
        if (existing != null) {
            return Result.error(400, "配置键已存在");
        }
        systemConfigService.save(config);

        saveLog("create", config.getId(), "新增系统配置: " + config.getConfigKey());
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    @RoleRequired(1)
    public Result<?> delete(@PathVariable Long id) {
        systemConfigService.removeById(id);

        saveLog("delete", id, "删除系统配置, id: " + id);
        return Result.success(null);
    }

    // ======== 私有辅助 ========

    private void saveLog(String action, Long targetId, String description) {
        try {
            Long operatorId = UserContext.getUserId();
            OperationLog oplog = new OperationLog();
            oplog.setUserId(operatorId);
            oplog.setModule("system_config");
            oplog.setAction(action);
            oplog.setTargetId(targetId);
            oplog.setDescription(description);
            oplog.setCreatedAt(LocalDateTime.now());
            operationLogService.save(oplog);
        } catch (Exception e) {
            log.error("操作日志写入失败: action={}, desc={}", action, description, e);
        }
    }
}
