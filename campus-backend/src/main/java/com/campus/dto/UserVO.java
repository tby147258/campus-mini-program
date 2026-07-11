package com.campus.dto;

import com.campus.entity.User;
import com.campus.enums.UserRole;
import com.campus.enums.UserStatus;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 用户响应视图 — 裁剪敏感字段（password、openId、isDeleted）
 */
@Getter
public class UserVO {
    private final Long id;
    private final String nickname;
    private final String avatar;
    private final String email;
    private final String phone;
    private final String studentNo;
    private final UserRole role;
    private final UserStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private UserVO(User user) {
        this.id = user.getId();
        this.nickname = user.getNickname();
        this.avatar = user.getAvatar();
        this.email = user.getEmail();
        this.phone = user.getPhone();
        this.studentNo = user.getStudentNo();
        this.role = user.getRole();
        this.status = user.getStatus();
        this.createdAt = user.getCreatedAt();
        this.updatedAt = user.getUpdatedAt();
    }

    public static UserVO from(User user) {
        return user == null ? null : new UserVO(user);
    }
}
