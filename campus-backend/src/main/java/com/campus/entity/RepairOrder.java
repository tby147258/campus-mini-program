package com.campus.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.campus.enums.RepairOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@TableName(value = "repair_order", autoResultMap = true)
public class RepairOrder {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 工单编号 */
    @TableField(value = "order_no")
    private String orderNo;

    /** 用户ID */
    @TableField(value = "user_id")
    private Long userId;

    /** 报修类型 */
    @TableField(value = "repair_type")
    private String repairType;

    /** 校区 */
    @TableField(value = "campus")
    private String campus;

    /** 楼栋 */
    @TableField(value = "building")
    private String building;

    /** 房间号 */
    @TableField(value = "room")
    private String room;

    /** 报修描述 */
    @TableField(value = "description")
    private String description;

    /** 图片JSON数组 */
    @TableField(value = "images", typeHandler = JacksonTypeHandler.class)
    private List<String> images;

    /** 联系人 */
    @TableField(value = "contact_person")
    private String contactPerson;

    /** 联系电话 */
    @TableField(value = "contact_phone")
    private String contactPhone;

    /** 状态（0=待处理，1=处理中，2=已完成，3=已驳回） */
    @TableField(value = "status")
    private RepairOrderStatus status;

    /** 处理人ID */
    @TableField(value = "handler_id")
    private Long handlerId;

    /** 驳回原因 */
    @TableField(value = "reject_reason")
    private String rejectReason;

    /** 处理结果 */
    @TableField(value = "handle_result")
    private String handleResult;

    /** 预计完成时间 */
    @TableField(value = "estimated_complete_time")
    private LocalDateTime estimatedCompleteTime;

    /** 处理时间 */
    @TableField(value = "handle_time", updateStrategy = FieldStrategy.ALWAYS)
    private LocalDateTime handleTime;

    /** 完成时间 */
    @TableField(value = "complete_time", updateStrategy = FieldStrategy.ALWAYS)
    private LocalDateTime completeTime;

    /** 逻辑删除标识 */
    @TableLogic
    @TableField(value = "is_deleted")
    private Integer isDeleted;

    /** 创建时间 */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}