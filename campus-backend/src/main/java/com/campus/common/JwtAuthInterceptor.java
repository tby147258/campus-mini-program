package com.campus.common;

import com.campus.annotation.NoAuth;
import com.campus.annotation.RoleRequired;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import org.springframework.core.annotation.AnnotationUtils;

@SuppressWarnings("null")
@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthInterceptor.class);

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    /** 完全公开路径（无需 Token，无需 @NoAuth） — 使用精确前缀匹配 */
    private static final String[] PUBLIC_WHITELIST = {"/api/file", "/error"};

    public JwtAuthInterceptor(JwtUtil jwtUtil, ObjectMapper objectMapper) {
        this.jwtUtil = jwtUtil;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        // 防御性清理：防止线程池复用残留上次请求上下文
        UserContext.clear();

        String path = request.getRequestURI();

        // 1. 白名单：精确前缀匹配（JK5）
        for (String white : PUBLIC_WHITELIST) {
            if (path.equals(white) || path.startsWith(white + "/")) {
                return true;
            }
        }

        // OPTIONS 预检请求放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 2. 解析方法+类级别注解信息
        boolean hasNoAuth = false;
        boolean hasRoleRequired = false;
        int requiredRole = 0;

        if (handler instanceof HandlerMethod hm) {
            // 方法注解优先，方法没有则查找类注解
            NoAuth noAuth = hm.getMethodAnnotation(NoAuth.class);
            if (noAuth != null) {
                hasNoAuth = true;
            } else {
                hasNoAuth = AnnotationUtils.findAnnotation(hm.getBeanType(), NoAuth.class) != null;
            }

            RoleRequired rr = hm.getMethodAnnotation(RoleRequired.class);
            if (rr != null) {
                hasRoleRequired = true;
                requiredRole = rr.value();
            } else {
                RoleRequired rrClass = AnnotationUtils.findAnnotation(hm.getBeanType(), RoleRequired.class);
                if (rrClass != null) {
                    hasRoleRequired = true;
                    requiredRole = rrClass.value();
                }
            }
        }

        // JK8: @NoAuth + @RoleRequired 共存冲突检测
        if (hasNoAuth && hasRoleRequired) {
            String methodName = handler instanceof HandlerMethod hm
                    ? hm.getMethod().getName() : "unknown";
            log.warn("方法 {} 同时标注 @NoAuth 和 @RoleRequired({})，语义矛盾！@RoleRequired 将生效",
                    methodName, requiredRole);
        }

        // 3. 尝试解析 Token
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                var claims = jwtUtil.parseToken(token);
                Long userId = Long.parseLong(claims.getSubject());
                Integer role = (Integer) claims.get("role");

                request.setAttribute("userId", userId);
                request.setAttribute("role", role);

                UserContext.set(userId, role);
            } catch (io.jsonwebtoken.ExpiredJwtException e) {
                writeJson(response, 401, "Token已过期，请重新登录");
                UserContext.clear();
                return false;
            } catch (io.jsonwebtoken.security.SecurityException e) {
                writeJson(response, 401, "Token签名无效");
                UserContext.clear();
                return false;
            } catch (Exception e) {
                writeJson(response, 401, "Token无效或格式错误");
                UserContext.clear();
                return false;
            }
        }

        // 4. 非 @NoAuth 路径 → 要求登录（JK7：统一逻辑，移除 AUTH_ME_PATH 特殊分支）
        if (!hasNoAuth) {
            Integer role = (Integer) request.getAttribute("role");
            if (role == null) {
                writeJson(response, 401, "未登录，请先登录");
                UserContext.clear();
                return false;
            }
        }

        // 5. 检查角色注解
        if (hasRoleRequired) {
            Integer role = (Integer) request.getAttribute("role");
            if (role == null) {
                writeJson(response, 401, "未登录，请先登录");
                UserContext.clear();
                return false;
            }
            if (role.intValue() < requiredRole) {
                writeJson(response, 403, "无权访问，权限不足");
                UserContext.clear();
                return false;
            }
        }

        return true;
    }

    private void writeJson(HttpServletResponse response, int code, String msg) throws Exception {
        Result<Void> result = Result.error(code, msg);
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(code);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                @NonNull Object handler, @Nullable Exception ex) {
        UserContext.clear();
    }
}