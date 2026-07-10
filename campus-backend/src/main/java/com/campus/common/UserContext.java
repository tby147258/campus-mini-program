package com.campus.common;

/**
 * 当前用户上下文（ThreadLocal）
 * 由 JwtInterceptor 在请求进入时设置，请求结束后清除
 */
public class UserContext {
    private static final ThreadLocal<Long> currentUserId = new ThreadLocal<>();
    private static final ThreadLocal<Integer> currentRole = new ThreadLocal<>();

    public static void set(Long userId, Integer role) {
        if (userId == null || role == null) {
            clear();
            return;
        }
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

    /**
     * 在异步/定时任务中手动设置上下文，配合 try-with-resources 自动清理：
     * <pre>
     * try (var ctx = UserContext.withContext(userId, role)) {
     *     // 执行业务代码
     * }
     * </pre>
     */
    public static AutoCloseable withContext(Long userId, Integer role) {
        set(userId, role);
        return UserContext::clear;
    }
}