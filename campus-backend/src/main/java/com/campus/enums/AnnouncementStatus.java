package com.campus.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 公告发布状态枚举
 */
@Getter
public enum AnnouncementStatus {
    DRAFT(0, "草稿"),
    PUBLISHED(1, "已发布");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String desc;

    AnnouncementStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @JsonCreator
    public static AnnouncementStatus fromCode(Integer code) {
        for (AnnouncementStatus s : values()) {
            if (s.code.equals(code)) {
                return s;
            }
        }
        return null;
    }
}
