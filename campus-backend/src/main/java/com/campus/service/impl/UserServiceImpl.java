package com.campus.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.entity.User;
import com.campus.mapper.UserMapper;
import com.campus.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public User loginOrRegister(String code) {
        // 实际开发中调用微信API获取openId
        // 此处为简化示例
        String openId = "mock_openid_" + code;
        User user = getByOpenId(openId);
        if (user == null) {
            user = new User();
            user.setOpenId(openId);
            user.setRole(0);
            user.setStatus(0);
            save(user);
        }
        return user;
    }

    @Override
    public User getByOpenId(String openId) {
        return lambdaQuery().eq(User::getOpenId, openId).one();
    }
}