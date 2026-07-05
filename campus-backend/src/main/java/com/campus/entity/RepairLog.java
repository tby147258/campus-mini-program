package com.campus.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("repair_log")
public class RepairLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;        // 关联工单ID
    private Long operatorId;     // 操作人ID
    private String action;       // 操作类型：submit/accept/reject/complete
    private Integer fromStatus;  // 操作前状态
    private Integer toStatus;    // 操作后状态
    private String remark;       // 操作备注

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}