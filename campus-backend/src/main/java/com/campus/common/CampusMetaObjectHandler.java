package com.campus.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

/**
 * MyBatis-Plus 自动填充处理器
 *
 * 自动填充字段：
 * - createdAt / updatedAt：创建时间 / 更新时间
 * - createdBy / updatedBy：创建人 / 更新人（从 UserContext 获取当前用户 ID）
 */
@Component
public class CampusMetaObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());

        Long userId = UserContext.getUserId();
        if (userId != null) {
            this.strictInsertFill(metaObject, "createdBy", Long.class, userId);
            this.strictInsertFill(metaObject, "updatedBy", Long.class, userId);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());

        Long userId = UserContext.getUserId();
        if (userId != null) {
            this.strictUpdateFill(metaObject, "updatedBy", Long.class, userId);
        }
    }
}