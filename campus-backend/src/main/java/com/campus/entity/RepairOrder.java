package com.campus.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.campus.enums.RepairOrderStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(of = "id")
@TableName("repair_order")
public class RepairOrder {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField(value = "order_no")
    private String orderNo;      // 工单编号
    @TableField(value = "repair_type")
    private String repairType;   // 电器/水暖/门窗/网络
    private String campus;       // 校区
    private String building;     // 楼栋
    private String room;         // 房间号
    private String description;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private String images;       // JSON数组字符串
    @TableField(value = "contact_person")
    private String contactPerson;
    @TableField(value = "contact_phone")
    private String contactPhone;
    private RepairOrderStatus status;  // PENDING-待处理, PROCESSING-处理中, COMPLETED-已完成, REJECTED-已驳回
    @TableField(value = "reject_reason")
    private String rejectReason;
    @TableField(value = "handle_result")
    private String handleResult;
    @TableField(value = "user_id")
    private Long userId;
    @TableField(value = "handler_id")
    private Long handlerId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "estimated_complete_time")
    private LocalDateTime estimatedCompleteTime;  // 预计完成时间，用于逾期提醒
    @TableField(value = "handle_time", updateStrategy = FieldStrategy.ALWAYS)
    private LocalDateTime handleTime;
    @TableField(value = "complete_time", updateStrategy = FieldStrategy.ALWAYS)
    private LocalDateTime completeTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer isDeleted;
}