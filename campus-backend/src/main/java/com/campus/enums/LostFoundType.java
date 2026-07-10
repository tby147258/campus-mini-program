package com.campus.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 失物招领类型枚举
 */
@Getter
public enum LostFoundType {
    FOUND(0, "失物招领"),
    LOOKING(1, "寻物启事");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String desc;

    LostFoundType(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @JsonCreator
    public static LostFoundType fromCode(Integer code) {
        for (LostFoundType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}
