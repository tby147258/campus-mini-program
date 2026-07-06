package com.campus.annotation;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RoleRequired {
    /**
     * 要求的角色：0-学生, 1-管理员
     * 方法级注解优先级高于类级注解
     */
    int value() default 1;
}
