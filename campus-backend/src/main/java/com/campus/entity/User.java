package com.campus.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.campus.enums.UserRole;
import com.campus.enums.UserStatus;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@TableName(value = "user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 微信OpenId */
    @TableField(value = "open_id")
    private String openId;

    /** 邮箱 */
    @TableField(value = "email")
    @Size(max = 128, message = "邮箱长度不能超过128个字符")
    private String email;

    /** 密码（BCrypt加密） */
    @TableField(value = "password")
    private String password;

    /** 昵称 */
    @TableField(value = "nickname")
    private String nickname;

    /** 头像URL */
    @TableField(value = "avatar")
    private String avatar;

    /** 手机号 */
    @TableField(value = "phone")
    private String phone;

    /** 学号 */
    @TableField(value = "student_no")
    private String studentNo;

    /** 角色（0=学生，1=管理员） */
    @TableField(value = "role")
    private UserRole role;

    /** 状态（0=正常，1=禁用） */
    @TableField(value = "status")
    private UserStatus status;

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