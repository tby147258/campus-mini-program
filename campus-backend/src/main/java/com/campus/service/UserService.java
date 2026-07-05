package com.campus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.entity.User;

public interface UserService extends IService<User> {
    User loginOrRegister(String code);
    User getByOpenId(String openId);
}