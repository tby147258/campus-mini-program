package com.campus.controller;

import com.campus.common.JwtUtil;
import com.campus.common.Result;
import com.campus.common.UserContext;
import com.campus.entity.OperationLog;
import com.campus.entity.User;
import com.campus.service.OperationLogService;
import com.campus.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;
    private final OperationLogService operationLogService;

    public AuthController(UserService userService, JwtUtil jwtUtil,
                          OperationLogService operationLogService) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.operationLogService = operationLogService;
    }

    /**
     * 微信小程序登录（模拟）
     */
    @PostMapping("/wx-login")
    public Result<?> wxLogin(@RequestBody Map<String, String> params) {
        String code = params.get("code");
        User user = userService.loginOrRegister(code);
        String token = jwtUtil.generateToken(user.getId(), user.getRole());
        user.setPassword(null);
        return Result.success(Map.of("token", token, "user", user));
    }

    /**
     * 管理员登录 - BCrypt密码校验 + DB查询
     */
    @PostMapping("/admin-login")
    public Result<?> adminLogin(@Valid @RequestBody AdminLoginRequest request,
                                HttpServletRequest httpRequest) {
        // 1. 按nickname查询管理员
        User user = userService.lambdaQuery()
                .eq(User::getNickname, request.getUsername())
                .eq(User::getRole, 1)       // 仅管理员
                .one();

        if (user == null) {
            return Result.error(401, "管理员用户不存在");
        }

        // 2. BCrypt密码校验
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            return Result.error(401, "该管理员未设置密码，请联系系统管理员");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return Result.error(401, "密码错误");
        }

        // 3. 检查状态
        if (user.getStatus() != 0) {
            return Result.error(403, "账号已被禁用，请联系系统管理员");
        }

        // 4. 生成JWT
        String token = jwtUtil.generateToken(user.getId(), user.getRole());

        // 5. 记录操作日志
        OperationLog log = new OperationLog();
        log.setUserId(user.getId());
        log.setModule("auth");
        log.setAction("login");
        log.setTargetId(user.getId());
        log.setDescription("管理员登录系统");
        log.setIpAddress(getClientIp(httpRequest));
        log.setCreatedAt(LocalDateTime.now());
        operationLogService.save(log);

        // 6. 返回token + 用户信息（不返回密码）
        user.setPassword(null);
        return Result.success(Map.of("token", token, "user", user));
    }

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/me")
    public Result<?> me() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        user.setPassword(null);
        return Result.success(user);
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理时取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 管理员登录请求体
     */
    public static class AdminLoginRequest {
        @NotBlank(message = "用户名不能为空")
        private String username;

        @NotBlank(message = "密码不能为空")
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}