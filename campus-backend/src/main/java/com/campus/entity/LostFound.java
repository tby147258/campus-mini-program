package com.campus.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.campus.enums.LostFoundStatus;
import com.campus.enums.LostFoundType;
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
@TableName(value = "lost_found", autoResultMap = true)
public class LostFound {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    @TableField(value = "user_id")
    private Long userId;

    /** 物品名称 */
    @TableField(value = "item_name")
    private String itemName;

    /** 类型（0=遗失，1=拾到） */
    @TableField(value = "type")
    private LostFoundType type;

    /** 分类 */
    @TableField(value = "category")
    private String category;

    /** 图片JSON数组 */
    @TableField(value = "images", typeHandler = JacksonTypeHandler.class)
    private List<String> images;

    /** 描述 */
    @TableField(value = "description")
    private String description;

    /** 地点 */
    @TableField(value = "location")
    private String location;

    /** 联系人 */
    @TableField(value = "contact_person")
    private String contactPerson;

    /** 联系电话 */
    @TableField(value = "contact_phone")
    private String contactPhone;

    /** 状态（0=待审核，1=已发布，2=已结束，3=已驳回） */
    @TableField(value = "status")
    private LostFoundStatus status;

    /** 审核人ID */
    @TableField(value = "auditor_id")
    private Long auditorId;

    /** 审核时间 */
    @TableField(value = "audit_time")
    private LocalDateTime auditTime;

    /** 驳回原因 */
    @TableField(value = "reject_reason")
    private String rejectReason;

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