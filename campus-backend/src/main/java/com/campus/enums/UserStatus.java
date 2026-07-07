package com.campus.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 用户账号状态枚举
 */
@Getter
public enum UserStatus {
    NORMAL(0, "正常"),
    DISABLED(1, "禁用");

    @EnumValue
    private final Integer code;
    private final String desc;

    UserStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static UserStatus fromCode(Integer code) {
        for (UserStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
}
