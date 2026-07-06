package com.campus.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.entity.SystemConfig;
import com.campus.mapper.SystemConfigMapper;
import com.campus.service.SystemConfigService;
import org.springframework.stereotype.Service;

@Service
public class SystemConfigServiceImpl extends ServiceImpl<SystemConfigMapper, SystemConfig> implements SystemConfigService {

    @Override
    public SystemConfig getByKey(String key) {
        return lambdaQuery().eq(SystemConfig::getConfigKey, key).one();
    }

    @Override
    public String getString(String key, String defaultVal) {
        SystemConfig config = getByKey(key);
        return config != null ? config.getConfigValue() : defaultVal;
    }
}
