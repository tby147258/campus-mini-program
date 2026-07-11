package com.campus.config;

import com.campus.common.JwtAuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final @NonNull JwtAuthInterceptor jwtAuthInterceptor;

    public WebMvcConfig(@NonNull JwtAuthInterceptor jwtAuthInterceptor) {
        this.jwtAuthInterceptor = jwtAuthInterceptor;
    }

    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping("/**")
                // allowCredentials(true) 时禁止通配 Origin，限定可信源
                // 前端开发端口：3000(React/Next.js)、5173(Vite)、8080(通用)
                // 127.0.0.1:* 覆盖小程序开发者工具本地代理端口
                .allowedOriginPatterns(
                        "http://localhost:3000", "https://localhost:3000",
                        "http://localhost:5173", "https://localhost:5173",
                        "http://localhost:8080", "https://localhost:8080",
                        "http://127.0.0.1:*", "https://127.0.0.1:*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("Authorization", "Content-Type", "X-Requested-With")
                .exposedHeaders("X-RateLimit-Remaining", "X-RateLimit-Reset")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(jwtAuthInterceptor)
                .addPathPatterns("/**")
                // 公开接口路径，非 @NoAuth 注解的纵深防御
                .excludePathPatterns("/api/captcha/**")
                // 静态资源路径
                .excludePathPatterns("/static/**", "/public/**", "/resources/**",
                        "/favicon.ico", "/error");
    }
}