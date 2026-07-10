package com.campus.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.campus.enums.RepairOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@TableName(value = "repair_log")
public class RepairLog {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 工单ID */
    @TableField(value = "order_id")
    private Long orderId;

    /** 操作人ID */
    @TableField(value = "operator_id")
    private Long operatorId;

    /** 操作类型 */
    @TableField(value = "action")
    private String action;

    /** 操作前状态 */
    @TableField(value = "from_status")
    private RepairOrderStatus fromStatus;

    /** 操作后状态 */
    @TableField(value = "to_status")
    private RepairOrderStatus toStatus;

    /** 备注 */
    @TableField(value = "remark")
    private String remark;

    /** 创建时间 */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}