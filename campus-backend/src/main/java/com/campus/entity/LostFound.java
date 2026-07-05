package com.campus.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("lost_found")
public class LostFound {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Integer type;       // 0-失物招领, 1-寻物启事
    private String itemName;
    private String category;
    private String images;      // JSON数组字符串
    private String description;
    private String location;
    private String contactPerson;
    private String contactPhone;
    private Integer status;     // 0-待审核, 1-已发布, 2-未通过, 3-已结束
    private Long userId;
    private Long auditorId;     // 审核人ID
    private LocalDateTime auditTime;
    private String rejectReason;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer isDeleted;
}