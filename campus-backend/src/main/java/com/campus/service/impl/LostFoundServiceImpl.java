package com.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.entity.LostFound;
import com.campus.enums.LostFoundStatus;
import com.campus.mapper.LostFoundMapper;
import com.campus.service.LostFoundService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class LostFoundServiceImpl extends ServiceImpl<LostFoundMapper, LostFound> implements LostFoundService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LostFound createLostFound(LostFound lostFound, Long userId) {
        // D15: 防御性空校验
        if (lostFound == null) {
            throw new IllegalArgumentException("失物招领记录不能为空");
        }
        lostFound.setUserId(userId);
        lostFound.setStatus(LostFoundStatus.PENDING_AUDIT);
        // D13: 防御性设置创建时间
        if (lostFound.getCreatedAt() == null) {
            lostFound.setCreatedAt(LocalDateTime.now());
        }
        save(lostFound);
        return lostFound;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateLostFound(Long id, LostFound req, Long currentUserId, boolean isAdmin) {
        if (id == null) {
            throw new IllegalArgumentException("ID 不能为空");
        }
        if (req == null) {
            throw new IllegalArgumentException("更新数据不能为空");
        }

        // D8: Service 层作为最后防线进行权属校验
        LostFound exist = getById(id);
        if (exist == null) {
            return false;
        }
        if (!isAdmin && !exist.getUserId().equals(currentUserId)) {
            throw new SecurityException("无权修改他人发布的记录");
        }

        // 白名单更新
        LambdaUpdateWrapper<LostFound> wrapper = new LambdaUpdateWrapper<LostFound>()
                .eq(LostFound::getId, id)
                .set(req.getItemName() != null, LostFound::getItemName, req.getItemName())
                .set(req.getCategory() != null, LostFound::getCategory, req.getCategory())
                .set(req.getDescription() != null, LostFound::getDescription, req.getDescription())
                .set(req.getLocation() != null, LostFound::getLocation, req.getLocation())
                .set(req.getContactPerson() != null, LostFound::getContactPerson, req.getContactPerson())
                .set(req.getContactPhone() != null, LostFound::getContactPhone, req.getContactPhone())
                .set(req.getImages() != null, LostFound::getImages, req.getImages());
        return update(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean auditLostFound(Long id, LostFoundStatus status, Long auditorId,
                                   String rejectReason) {
        if (id == null) {
            throw new IllegalArgumentException("ID 不能为空");
        }
        if (status == null) {
            throw new IllegalArgumentException("审核状态不能为空");
        }
        LostFound exist = getById(id);
        if (exist == null) {
            return false;
        }

        LambdaUpdateWrapper<LostFound> wrapper = new LambdaUpdateWrapper<LostFound>()
                .eq(LostFound::getId, id)
                .set(LostFound::getStatus, status)
                .set(LostFound::getAuditorId, auditorId)
                .set(LostFound::getAuditTime, LocalDateTime.now());
        if (status == LostFoundStatus.REJECTED && rejectReason != null) {
            wrapper.set(LostFound::getRejectReason, rejectReason);
        }
        return update(wrapper);
    }
}