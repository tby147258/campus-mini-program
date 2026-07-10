package com.campus.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 失物招领审核状态枚举
 */
@Getter
public enum LostFoundStatus {
    PENDING_AUDIT(0, "待审核"),
    PUBLISHED(1, "已发布"),
    REJECTED(2, "未通过"),
    CLOSED(3, "已结束");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String desc;

    LostFoundStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @JsonCreator
    public static LostFoundStatus fromCode(Integer code) {
        for (LostFoundStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
}
