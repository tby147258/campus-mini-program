package com.campus.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.entity.User;
import com.campus.enums.UserRole;
import com.campus.enums.UserStatus;
import com.campus.mapper.UserMapper;
import com.campus.service.UserService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    @Cacheable(value = "user", key = "#id")
    public User getById(Serializable id) {
        User user = super.getById(id);
        if (user != null) {
            user.setPassword(null); // US2: 缓存前脱敏，避免 BCrypt 哈希泄露到 Redis
        }
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // US1: 事务保护查-插原子性
    public User loginOrRegister(String code) {
        // US4: 参数非空校验
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("code 不能为空");
        }
        String openId = "mock_openid_" + code;
        User user = getByOpenId(openId);
        if (user == null) {
            try {
                user = new User();
                user.setOpenId(openId);
                user.setRole(UserRole.STUDENT);
                user.setStatus(UserStatus.NORMAL);
                save(user);
            } catch (DuplicateKeyException e) {
                // US6: 唯一键冲突 → 并发插入，重试查询
                user = getByOpenId(openId);
                if (user == null) {
                    throw e; // 仍然获取不到则向上抛
                }
            }
        }
        return user;
    }

    @Override
    public User getByOpenId(String openId) {
        // US5: 参数非空校验
        if (openId == null || openId.isBlank()) {
            return null;
        }
        // US6: 防御性查询，防止多记录时 .one() 抛 TooManyResultsException
        return lambdaQuery().eq(User::getOpenId, openId).last("LIMIT 1").one();
    }

    @Override
    @CacheEvict(value = "user", key = "#entity.id") // US3: 更新时清除缓存
    public boolean updateById(User entity) {
        // 更新时不清除密码，仅清除缓存
        return super.updateById(entity);
    }

    @Override
    @CacheEvict(value = "user", key = "#id") // US3: 删除时清除缓存
    public boolean removeById(Serializable id) {
        return super.removeById(id);
    }
}