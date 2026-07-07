package com.campus.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.campus.enums.LostFoundStatus;
import com.campus.enums.LostFoundType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(of = "id")
@TableName("lost_found")
public class LostFound {
    @TableId(type = IdType.AUTO)
    private Long id;
    private LostFoundType type;  // LOST-失物招领, FOUND-寻物启事
    @TableField(value = "item_name")
    private String itemName;
    private String category;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private String images;      // JSON数组字符串
    private String description;
    private String location;
    @TableField(value = "contact_person")
    private String contactPerson;
    @TableField(value = "contact_phone")
    private String contactPhone;
    private LostFoundStatus status;  // PENDING_AUDIT-待审核, PUBLISHED-已发布, REJECTED-未通过, CLOSED-已结束
    @TableField(value = "user_id")
    private Long userId;
    @TableField(value = "auditor_id")
    private Long auditorId;     // 审核人ID
    @TableField(value = "audit_time")
    private LocalDateTime auditTime;
    @TableField(value = "reject_reason")
    private String rejectReason;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer isDeleted;
}