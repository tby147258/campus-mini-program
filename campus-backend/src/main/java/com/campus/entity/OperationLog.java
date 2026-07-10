package com.campus.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@TableName(value = "operation_log")
public class OperationLog {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    @TableField(value = "user_id")
    private Long userId;

    /** 操作模块 */
    @TableField(value = "module")
    private String module;

    /** 操作类型 */
    @TableField(value = "action")
    private String action;

    /** 操作目标ID */
    @TableField(value = "target_id")
    private Long targetId;

    /** 操作描述 */
    @TableField(value = "description")
    private String description;

    /** IP地址 */
    @TableField(value = "ip_address")
    private String ipAddress;

    /** 创建时间 */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}