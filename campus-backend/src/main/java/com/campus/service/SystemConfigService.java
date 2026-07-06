package com.campus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.entity.SystemConfig;

public interface SystemConfigService extends IService<SystemConfig> {
    SystemConfig getByKey(String key);
    String getString(String key, String defaultVal);
}
