package com.campus.controller;

import com.campus.common.JwtUtil;
import com.campus.common.Result;
import com.campus.entity.User;
import com.campus.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;
    private final JwtUtil jwtUtil;

    public AuthController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/wx-login")
    public Result<?> wxLogin(@RequestBody Map<String, String> params) {
        String code = params.get("code");
        User user = userService.loginOrRegister(code);
        String token = jwtUtil.generateToken(user.getId(), user.getRole());
        return Result.success(Map.of("token", token, "user", user));
    }

    @PostMapping("/admin-login")
    public Result<?> adminLogin(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");
        // 简化校验，实际开发需查询数据库
        if ("admin".equals(username) && "admin123".equals(password)) {
            String token = jwtUtil.generateToken(1L, 1);
            return Result.success(Map.of("token", token));
        }
        return Result.error(401, "用户名或密码错误");
    }
}