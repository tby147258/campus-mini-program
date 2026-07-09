package com.campus.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.annotation.RoleRequired;
import com.campus.common.Result;
import com.campus.entity.SystemConfig;
import com.campus.service.SystemConfigService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/system-config")
@SuppressWarnings("null")
public class SystemConfigController {
    private final SystemConfigService systemConfigService;

    public SystemConfigController(SystemConfigService systemConfigService) {
        this.systemConfigService = systemConfigService;
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
        return Result.success(null);
    }

    @PutMapping("/batch")
    @RoleRequired(1)
    public Result<?> batchUpdate(@RequestBody List<SystemConfig> configs) {
        systemConfigService.updateBatchById(configs);
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
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    @RoleRequired(1)
    public Result<?> delete(@PathVariable Long id) {
        systemConfigService.removeById(id);
        return Result.success(null);
    }
}
