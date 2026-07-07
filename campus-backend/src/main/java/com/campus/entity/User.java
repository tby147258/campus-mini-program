package com.campus.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.campus.enums.UserRole;
import com.campus.enums.UserStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(of = "id")
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField(value = "open_id")
    private String openId;
    private String email;
    @com.fasterxml.jackson.annotation.JsonIgnore
    private String password;    // BCrypt加密，仅管理员使用
    private String nickname;
    private String avatar;
    @TableField(value = "student_no")
    private String studentNo;
    private String phone;
    private UserRole role;       // 角色：STUDENT-学生, ADMIN-管理员
    private UserStatus status;   // 状态：NORMAL-正常, DISABLED-禁用

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer isDeleted;
}