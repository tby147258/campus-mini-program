package com.campus.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
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
    private final Integer code;
    private final String desc;

    LostFoundStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static LostFoundStatus fromCode(Integer code) {
        for (LostFoundStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
}
