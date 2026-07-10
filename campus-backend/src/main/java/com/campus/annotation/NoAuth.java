package com.campus.annotation;

import java.lang.annotation.*;

/**
 * 标记不需要登录认证的接口（公开API）
 * 可加在 Controller 类或方法上，拦截器会跳过 Token 校验
 * 方法级注解优先级高于类级注解
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NoAuth {
}