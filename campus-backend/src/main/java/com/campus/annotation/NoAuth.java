package com.campus.annotation;

import java.lang.annotation.*;

/**
 * 标记不需要登录认证的接口（公开API）
 * 加在 Controller 方法上，拦截器会跳过 Token 校验
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NoAuth {
}