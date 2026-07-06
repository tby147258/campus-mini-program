package com.campus.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    private static final String[] WHITELIST = {"/api/auth/", "/api/files/", "/error"};

    public JwtAuthInterceptor(JwtUtil jwtUtil, ObjectMapper objectMapper) {
        this.jwtUtil = jwtUtil;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 白名单路径跳过
        String path = request.getRequestURI();
        for (String white : WHITELIST) {
            if (path.startsWith(white)) {
                return true;
            }
        }

        // OPTIONS 预检请求放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 提取 Bearer token
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            writeJson(response, 401, "未登录或Token格式错误");
            return false;
        }

        String token = authHeader.substring(7);

        try {
            var claims = jwtUtil.parseToken(token);
            Long userId = Long.parseLong(claims.getSubject());
            Integer role = (Integer) claims.get("role");

            request.setAttribute("userId", userId);
            request.setAttribute("role", role);

            // 设置 ThreadLocal 用户上下文
            UserContext.set(userId, role);

            // 检查角色注解
            if (handler instanceof HandlerMethod hm) {
                RoleRequired roleRequired = hm.getMethodAnnotation(RoleRequired.class);
                if (roleRequired == null) {
                    roleRequired = hm.getBeanType().getAnnotation(RoleRequired.class);
                }
                if (roleRequired != null && role != null && role.intValue() != roleRequired.value()) {
                    writeJson(response, 403, "无权访问，权限不足");
                    return false;
                }
            }

            return true;

        } catch (Exception e) {
            writeJson(response, 401, "Token无效或已过期");
            return false;
        }
    }

    private void writeJson(HttpServletResponse response, int code, String msg) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(200);
        response.getWriter().write(objectMapper.writeValueAsString(
                Map.of("code", code, "msg", msg, "data", null)));
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        UserContext.clear();
    }
}
