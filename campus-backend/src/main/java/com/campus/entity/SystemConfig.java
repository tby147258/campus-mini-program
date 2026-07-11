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
@TableName(value = "system_config")
public class SystemConfig {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 配置键 */
    @TableField(value = "config_key")
    private String configKey;

    /** 配置值 */
    @TableField(value = "config_value")
    private String configValue;

    /** 配置描述 */
    @TableField(value = "description")
    private String description;

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