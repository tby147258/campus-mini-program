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
@TableName(value = "announcement")
public class Announcement {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 标题 */
    @TableField(value = "title")
    private String title;

    /** 内容 */
    @TableField(value = "content")
    private String content;

    /** 发布人ID */
    @TableField(value = "publisher_id")
    private Long publisherId;

    /** 浏览次数 */
    @TableField(value = "view_count")
    private Integer viewCount;

    /** 状态（0=草稿，1=已发布） */
    @TableField(value = "status")
    private Integer status;

    /** 分类（教务通知/活动通知/紧急通知） */
    @TableField(value = "category")
    private String category;

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