package com.campus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.entity.LostFound;
import com.campus.enums.LostFoundStatus;

public interface LostFoundService extends IService<LostFound> {

    /**
     * 创建失物招领记录（含事务保护）
     */
    LostFound createLostFound(LostFound lostFound, Long userId);

    /**
     * 更新失物招领记录（含事务保护 + 权属校验）
     * @param currentUserId 当前登录用户 ID
     * @param isAdmin 是否为管理员
     * @return true 更新成功，false 记录不存在
     */
    boolean updateLostFound(Long id, LostFound req, Long currentUserId, boolean isAdmin);

    /**
     * 审核失物招领记录（含事务保护）
     */
    boolean auditLostFound(Long id, LostFoundStatus status, Long auditorId,
                           String rejectReason);
}