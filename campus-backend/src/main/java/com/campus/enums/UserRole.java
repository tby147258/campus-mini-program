package com.campus.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 用户角色枚举
 */
@Getter
public enum UserRole {
    STUDENT(0, "学生"),
    ADMIN(1, "管理员");

    @EnumValue
    private final Integer code;
    private final String desc;

    UserRole(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static UserRole fromCode(Integer code) {
        for (UserRole role : values()) {
            if (role.code.equals(code)) {
                return role;
            }
        }
        return null;
    }
}
