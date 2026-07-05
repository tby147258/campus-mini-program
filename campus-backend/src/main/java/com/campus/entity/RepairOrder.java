package com.campus.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("repair_order")
public class RepairOrder {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;      // 工单编号
    private String repairType;   // 电器/水暖/门窗/网络
    private String campus;       // 校区
    private String building;     // 楼栋
    private String room;         // 房间号
    private String description;
    private String images;       // JSON数组字符串
    private String contactPhone;
    private Integer status;      // 0-待处理, 1-处理中, 2-已完成, 3-已驳回
    private String rejectReason;
    private String handleResult;
    private Long userId;
    private Long handlerId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    private LocalDateTime handleTime;
    private LocalDateTime completeTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer isDeleted;
}