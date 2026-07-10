package com.campus.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 报修工单状态枚举
 */
@Getter
public enum RepairOrderStatus {
    PENDING(0, "待处理"),
    PROCESSING(1, "处理中"),
    COMPLETED(2, "已完成"),
    REJECTED(3, "已驳回");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String desc;

    RepairOrderStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @JsonCreator
    public static RepairOrderStatus fromCode(Integer code) {
        for (RepairOrderStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
}
