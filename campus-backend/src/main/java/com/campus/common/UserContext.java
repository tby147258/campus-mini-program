package com.campus.common;

/**
 * 当前用户上下文（ThreadLocal）
 * 由 JwtInterceptor 在请求进入时设置，请求结束后清除
 */
public class UserContext {
    private static final ThreadLocal<Long> currentUserId = new ThreadLocal<>();
    private static final ThreadLocal<Integer> currentRole = new ThreadLocal<>();

    public static void set(Long userId, Integer role) {
        currentUserId.set(userId);
        currentRole.set(role);
    }

    public static Long getUserId() {
        return currentUserId.get();
    }

    public static Integer getRole() {
        return currentRole.get();
    }

    public static void clear() {
        currentUserId.remove();
        currentRole.remove();
    }
}
