package com.campus.controller;

import com.campus.annotation.NoAuth;
import com.campus.common.JwtUtil;
import com.campus.common.Result;
import com.campus.common.UserContext;
import com.campus.entity.OperationLog;
import com.campus.entity.User;
import com.campus.enums.UserRole;
import com.campus.enums.UserStatus;
import com.campus.service.CaptchaService;
import com.campus.service.OperationLogService;
import com.campus.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;
    private final OperationLogService operationLogService;
    private final CaptchaService captchaService;
    private final StringRedisTemplate redisTemplate;

    public AuthController(UserService userService, JwtUtil jwtUtil,
                          BCryptPasswordEncoder passwordEncoder,
                          OperationLogService operationLogService,
                          CaptchaService captchaService,
                          StringRedisTemplate redisTemplate) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.operationLogService = operationLogService;
        this.captchaService = captchaService;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 微信小程序登录（模拟）
     */
    @PostMapping("/wx-login")
    @NoAuth
    public Result<?> wxLogin(@RequestBody Map<String, String> params) {
        String code = params.get("code");
        User user = userService.loginOrRegister(code);
        String token = jwtUtil.generateToken(user.getId(), user.getRole());
        user.setPassword(null);
        return Result.success(Map.of("token", token, "user", user));
    }

    /**
     * 管理员登录（含滑块验证码校验）
     */
    @PostMapping("/admin-login")
    @NoAuth
    public Result<?> adminLogin(@Valid @RequestBody AdminLoginRequest request,
                                HttpServletRequest httpRequest) {
        // 0. 校验滑块验证码 passToken（Redis 中验证）
        if (request.getPassToken() == null || request.getPassToken().isBlank()) {
            return Result.error(400, "请完成滑块验证");
        }
        String passKey = "captcha_pass:" + request.getPassToken();
        String passValid = redisTemplate.opsForValue().get(passKey);
        if (passValid == null) {
            return Result.error(400, "验证已过期，请重新验证");
        }
        redisTemplate.delete(passKey); // 一次性使用
        User user = userService.lambdaQuery()
                .eq(User::getNickname, request.getUsername())
                .eq(User::getRole, UserRole.ADMIN)
                .one();

        if (user == null) {
            return Result.error(401, "管理员用户不存在");
        }

        if (user.getPassword() == null || user.getPassword().isBlank()) {
            return Result.error(401, "该管理员未设置密码");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return Result.error(401, "密码错误");
        }
        if (user.getStatus() != UserStatus.NORMAL) {
            return Result.error(403, "账号已被禁用");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getRole());

        OperationLog log = new OperationLog();
        log.setUserId(user.getId());
        log.setModule("auth");
        log.setAction("login");
        log.setTargetId(user.getId());
        log.setDescription("管理员登录系统");
        log.setIpAddress(getClientIp(httpRequest));
        log.setCreatedAt(LocalDateTime.now());
        operationLogService.save(log);

        user.setPassword(null);
        return Result.success(Map.of("token", token, "user", user));
    }

    /**
     * 用户注册（学生端）
     */
    @PostMapping("/register")
    @NoAuth
    public Result<?> register(@Valid @RequestBody RegisterRequest request) {
        // 校验邮箱唯一性
        User exist = userService.lambdaQuery().eq(User::getEmail, request.getEmail()).one();
        if (exist != null) {
            return Result.error(400, "该邮箱已被注册");
        }

        // 生成唯一 openId（D12：使用 UUID 避免高并发重复）
        String openId = "reg_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        User user = new User();
        user.setOpenId(openId);
        user.setEmail(request.getEmail());
        user.setNickname(request.getNickname());
        user.setPhone(request.getPhone());
        user.setStudentNo(request.getStudentNo());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.STUDENT); // 默认为学生
        user.setStatus(UserStatus.NORMAL);
        userService.save(user);

        String token = jwtUtil.generateToken(user.getId(), user.getRole());
        user.setPassword(null);
        return Result.success(Map.of("token", token, "user", user));
    }

    /**
     * 发送重置密码验证码到邮箱
     */
    @PostMapping("/forgot-password")
    @NoAuth
    public Result<?> forgotPassword(@RequestParam String email) {
        User user = userService.lambdaQuery().eq(User::getEmail, email).one();
        if (user == null) {
            return Result.error(404, "该邮箱未注册");
        }
        // D7: 速率限制 — 同一邮箱 60 秒内仅允许发送 1 次
        String rateKey = "rate:forgot_pwd:" + email;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(rateKey))) {
            return Result.error(429, "请求过于频繁，请 60 秒后再试");
        }
        redisTemplate.opsForValue().set(rateKey, "1", 60, TimeUnit.SECONDS);

        // D8: 传入场景标识
        captchaService.sendEmailCode(email, "forgot-password");
        return Result.success(null);
    }

    /**
     * 重置密码
     */
    @PostMapping("/reset-password")
    @NoAuth
    public Result<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        // D8: 验证码校验传入场景标识
        boolean ok = captchaService.verifyEmailCode(request.getEmail(), "forgot-password", request.getCode());
        if (!ok) {
            return Result.error(400, "验证码错误或已过期");
        }
        User user = userService.lambdaQuery().eq(User::getEmail, request.getEmail()).one();
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userService.updateById(user);
        return Result.success(null);
    }

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

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    // ======== 内部 DTO ========

    public static class AdminLoginRequest {
        @NotBlank(message = "用户名不能为空")
        private String username;
        @NotBlank(message = "密码不能为空")
        private String password;
        private String passToken;   // 滑块验证通过后的凭证

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getPassToken() { return passToken; }
        public void setPassToken(String passToken) { this.passToken = passToken; }
    }

    public static class RegisterRequest {
        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱格式不正确")
        private String email;
        @NotBlank(message = "密码不能为空")
        private String password;
        @NotBlank(message = "昵称不能为空")
        private String nickname;
        private String phone;
        private String studentNo;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getNickname() { return nickname; }
        public void setNickname(String nickname) { this.nickname = nickname; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getStudentNo() { return studentNo; }
        public void setStudentNo(String studentNo) { this.studentNo = studentNo; }
    }

    public static class ResetPasswordRequest {
        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱格式不正确")
        private String email;
        @NotBlank(message = "验证码不能为空")
        private String code;
        @NotBlank(message = "新密码不能为空")
        private String newPassword;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }
}