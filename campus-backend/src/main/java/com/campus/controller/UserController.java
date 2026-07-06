package com.campus.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.annotation.RoleRequired;
import com.campus.common.Result;
import com.campus.common.UserContext;
import com.campus.dto.CreateUserRequest;
import com.campus.entity.User;
import com.campus.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RoleRequired(1)
public class UserController {

    private final UserService userService;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserController(UserService userService) {
        this.userService = userService;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    /**
     * 分页列表 - 支持角色筛选、状态筛选、关键字模糊搜索(nickname/phone/student_no)
     */
    @GetMapping("/page")
    public Result<?> page(@RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int size,
                          @RequestParam(required = false) Integer role,
                          @RequestParam(required = false) Integer status,
                          @RequestParam(required = false) String keyword) {
        Page<User> p = new Page<>(page, size);
        LambdaQueryWrapper<User> q = new LambdaQueryWrapper<>();
        q.eq(role != null, User::getRole, role)
         .eq(status != null, User::getStatus, status)
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
        user.setRole(req.getRole());
        user.setStatus(0); // 默认启用

        userService.save(user);

        // 操作日志
        Long operatorId = UserContext.getUserId();
        System.out.printf("[USER_CREATE] operator=%d, target=%d, role=%d%n",
                operatorId, user.getId(), user.getRole());

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

        Long operatorId = UserContext.getUserId();
        System.out.printf("[USER_UPDATE] operator=%d, target=%d%n", operatorId, id);

        user.setPassword(null);
        return Result.success(user);
    }

    /**
     * 修改用户状态（启用/禁用）
     */
    @PutMapping("/{id}/status")
    public Result<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Integer status = body.get("status");
        if (status == null || (status != 0 && status != 1)) {
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

        Long operatorId = UserContext.getUserId();
        System.out.printf("[USER_STATUS] operator=%d, target=%d, status=%d%n",
                operatorId, id, status);

        return Result.success(Map.of("id", id, "status", status));
    }

    /**
     * 重置密码 - 生成随机密码并BCrypt加密
     */
    @PutMapping("/{id}/password")
    public Result<?> resetPassword(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        // 生成8位随机密码
        String newPassword = generateRandomPassword(8);
        String encoded = passwordEncoder.encode(newPassword);

        userService.lambdaUpdate()
                .eq(User::getId, id)
                .set(User::getPassword, encoded)
                .update();

        Long operatorId = UserContext.getUserId();
        System.out.printf("[USER_PASSWORD_RESET] operator=%d, target=%d%n", operatorId, id);

        // 返回新密码（仅此一次可见）
        return Result.success(Map.of("id", id, "newPassword", newPassword));
    }

    /**
     * 修改密码 - 用户指定新密码
     */
    @PutMapping("/{id}/change-password")
    public Result<?> changePassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String newPassword = body.get("password");
        if (newPassword == null || newPassword.length() < 6) {
            return Result.error(400, "密码至少6位");
        }
        String encoded = passwordEncoder.encode(newPassword);
        userService.lambdaUpdate()
                .eq(User::getId, id)
                .set(User::getPassword, encoded)
                .update();
        return Result.success("密码修改成功");
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

        Long operatorId = UserContext.getUserId();
        System.out.printf("[USER_DELETE] operator=%d, target=%d%n", operatorId, id);

        return Result.success(null);
    }

    /**
     * 批量删除（逻辑删除）
     */
    @DeleteMapping("/batch")
    public Result<?> deleteBatch(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.error(400, "请选择要删除的用户");
        }

        userService.removeByIds(ids);

        Long operatorId = UserContext.getUserId();
        System.out.printf("[USER_BATCH_DELETE] operator=%d, targets=%s, count=%d%n",
                operatorId, ids, ids.size());

        return Result.success(Map.of("deleted", ids.size()));
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
