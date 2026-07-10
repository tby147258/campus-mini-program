package com.campus.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.annotation.RoleRequired;
import com.campus.common.Result;
import com.campus.common.UserContext;
import com.campus.dto.CreateUserRequest;
import com.campus.entity.OperationLog;
import com.campus.entity.User;
import com.campus.enums.UserRole;
import com.campus.enums.UserStatus;
import com.campus.service.OperationLogService;
import com.campus.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RoleRequired(1)
@SuppressWarnings("null")
public class UserController {

    private final UserService userService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final OperationLogService operationLogService;

    public UserController(UserService userService, BCryptPasswordEncoder passwordEncoder,
                          OperationLogService operationLogService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.operationLogService = operationLogService;
    }

    /**
     * 分页列表 - 支持角色筛选、状态筛选、关键字模糊搜索(nickname/phone/student_no)
     */
    @GetMapping
    public Result<?> page(@RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int size,
                          @RequestParam(required = false) Integer role,
                          @RequestParam(required = false) Integer status,
                          @RequestParam(required = false) String keyword) {
        Page<User> p = new Page<>(page, size);
        LambdaQueryWrapper<User> q = new LambdaQueryWrapper<>();
        q.eq(role != null, User::getRole, role != null ? UserRole.fromCode(role) : null)
         .eq(status != null, User::getStatus, status != null ? UserStatus.fromCode(status) : null)
         .and(keyword != null && !keyword.isBlank(), w -> w
                 .like(User::getNickname, keyword)
                 .or()
                 .like(User::getPhone, keyword)
                 .or()
                 .like(User::getStudentNo, keyword))
         .orderByDesc(User::getCreatedAt);
        return Result.success(userService.page(p, q));
    }

    /**
     * 查询用户详情
     */
    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        return Result.success(user);
    }

    /**
     * 新增用户 - 管理员创建
     */
    @PostMapping
    public Result<?> create(@Valid @RequestBody CreateUserRequest req) {
        // D21: 检查邮箱唯一性
        if (req.getEmail() != null && !req.getEmail().isBlank()) {
            long emailCount = userService.lambdaQuery()
                    .eq(User::getEmail, req.getEmail())
                    .count();
            if (emailCount > 0) {
                return Result.error(400, "该邮箱已被使用");
            }
        }

        // 检查手机号唯一性
        if (req.getPhone() != null && !req.getPhone().isBlank()) {
            long count = userService.lambdaQuery()
                    .eq(User::getPhone, req.getPhone())
                    .count();
            if (count > 0) {
                return Result.error(400, "手机号已被使用");
            }
        }

        User user = new User();
        // openId: 未提供则自动生成 admin_create_ + UUID前8位
        if (req.getOpenId() != null && !req.getOpenId().isBlank()) {
            user.setOpenId(req.getOpenId());
        } else {
            user.setOpenId("admin_create_" + UUID.randomUUID().toString().substring(0, 8));
        }
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setNickname(req.getNickname());
        user.setStudentNo(req.getStudentNo());
        user.setPhone(req.getPhone());
        user.setRole(UserRole.fromCode(req.getRole()));
        user.setStatus(UserStatus.NORMAL); // 默认启用

        userService.save(user);

        // D12: 持久化操作日志（替代 System.out.printf）
        saveOperationLog("user", "create", user.getId(),
                "创建用户: " + user.getNickname());

        // 返回时不包含密码（@JsonIgnore已处理，此处再确保置空）
        user.setPassword(null);
        return Result.success(user);
    }

    /**
     * 修改用户信息（昵称、手机号、学号等）
     */
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody User req) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        // 手机号唯一性检查
        if (req.getPhone() != null && !req.getPhone().isBlank()
                && !req.getPhone().equals(user.getPhone())) {
            long count = userService.lambdaQuery()
                    .eq(User::getPhone, req.getPhone())
                    .count();
            if (count > 0) {
                return Result.error(400, "手机号已被使用");
            }
        }

        // 仅更新允许修改的字段
        if (req.getNickname() != null) user.setNickname(req.getNickname());
        if (req.getStudentNo() != null) user.setStudentNo(req.getStudentNo());
        if (req.getPhone() != null) user.setPhone(req.getPhone());
        if (req.getAvatar() != null) user.setAvatar(req.getAvatar());

        userService.updateById(user);

        saveOperationLog("user", "update", id,
                "更新用户信息");

        user.setPassword(null);
        return Result.success(user);
    }

    /**
     * 修改用户状态（启用/禁用）
     */
    @PutMapping("/{id}/status")
    public Result<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Integer statusCode = body.get("status");
        if (statusCode == null) {
            return Result.error(400, "状态值不能为空");
        }
        UserStatus status = UserStatus.fromCode(statusCode);
        if (status == null) {
            return Result.error(400, "状态值无效，应为0(启用)或1(禁用)");
        }

        User user = userService.getById(id);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        userService.lambdaUpdate()
                .eq(User::getId, id)
                .set(User::getStatus, status)
                .update();

        saveOperationLog("user", "update_status", id,
                "修改用户状态为: " + status.getDesc());

        return Result.success(Map.of("id", id, "status", status.getCode()));
    }

    /**
     * 重置密码 - 生成随机密码并BCrypt加密，或管理员指定新密码
     */
    @PutMapping("/{id}/password")
    public Result<?> resetOrChangePassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        String newPassword = body.get("password");
        if (newPassword != null && !newPassword.isBlank()) {
            // 管理员指定密码模式
            if (newPassword.length() < 6) {
                return Result.error(400, "密码至少6位");
            }
            String encoded = passwordEncoder.encode(newPassword);
            userService.lambdaUpdate()
                    .eq(User::getId, id)
                    .set(User::getPassword, encoded)
                    .update();

            saveOperationLog("user", "change_password", id,
                    "管理员修改用户密码");

            // D27: 返回结构化数据（与 resetPassword 统一风格）
            return Result.success(Map.of("id", id, "message", "密码修改成功"));
        } else {
            // 自动生成随机密码模式（原 resetPassword）
            newPassword = generateRandomPassword(8);
            String encoded = passwordEncoder.encode(newPassword);

            userService.lambdaUpdate()
                    .eq(User::getId, id)
                    .set(User::getPassword, encoded)
                    .update();

            saveOperationLog("user", "reset_password", id,
                    "管理员重置用户密码");

            // 返回新密码（仅此一次可见）
            return Result.success(Map.of("id", id, "newPassword", newPassword));
        }
    }

    /**
     * 删除用户（逻辑删除）
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        userService.removeById(id);

        saveOperationLog("user", "delete", id,
                "删除用户: " + user.getNickname());

        return Result.success(null);
    }

    /**
     * 批量删除（逻辑删除）
     */
    @PostMapping("/batch-delete")
    public Result<?> deleteBatch(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.error(400, "请选择要删除的用户");
        }

        userService.removeByIds(ids);

        saveOperationLog("user", "batch_delete", null,
                "批量删除用户，数量: " + ids.size());

        return Result.success(Map.of("deleted", ids.size()));
    }

    // ======== 私有辅助方法 ========

    /**
     * 保存操作日志（D12: 替代 System.out.printf）
     */
    private void saveOperationLog(String module, String action, Long targetId, String description) {
        Long operatorId = UserContext.getUserId();
        OperationLog log = new OperationLog();
        log.setUserId(operatorId);
        log.setModule(module);
        log.setAction(action);
        log.setTargetId(targetId);
        log.setDescription(description);
        log.setIpAddress(null); // 可后续扩展获取IP
        log.setCreatedAt(LocalDateTime.now());
        operationLogService.save(log);
    }

    /**
     * 生成随机密码
     */
    private String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}