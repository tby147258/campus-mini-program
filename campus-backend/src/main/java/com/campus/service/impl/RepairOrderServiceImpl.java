package com.campus.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.entity.RepairOrder;
import com.campus.enums.RepairOrderStatus;
import com.campus.mapper.RepairOrderMapper;
import com.campus.service.RepairOrderService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class RepairOrderServiceImpl extends ServiceImpl<RepairOrderMapper, RepairOrder> implements RepairOrderService {

    /** 工单编号生成重试次数 */
    private static final int ORDER_NO_MAX_RETRIES = 3;

    /** 合法状态流转规则 */
    private static final Map<RepairOrderStatus, RepairOrderStatus[]> VALID_TRANSITIONS = Map.of(
            RepairOrderStatus.PENDING,    new RepairOrderStatus[]{RepairOrderStatus.PROCESSING, RepairOrderStatus.REJECTED},
            RepairOrderStatus.PROCESSING, new RepairOrderStatus[]{RepairOrderStatus.COMPLETED, RepairOrderStatus.REJECTED},
            RepairOrderStatus.REJECTED,   new RepairOrderStatus[]{RepairOrderStatus.PENDING},
            RepairOrderStatus.COMPLETED,  new RepairOrderStatus[]{} // 终态，不可流转
    );

    @Override
    @Transactional(rollbackFor = Exception.class) // RS4: 工单创建事务保护
    public RepairOrder createOrder(RepairOrder repairOrder, Long userId) {
        // D15: 防御性空校验 — Service 层作为独立可复用单元
        if (repairOrder == null) {
            throw new IllegalArgumentException("工单信息不能为空");
        }
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (repairOrder.getRepairType() == null || repairOrder.getRepairType().isBlank()) {
            throw new IllegalArgumentException("报修类型不能为空");
        }
        if (repairOrder.getDescription() == null || repairOrder.getDescription().isBlank()) {
            throw new IllegalArgumentException("报修描述不能为空");
        }

        repairOrder.setUserId(userId);
        repairOrder.setStatus(RepairOrderStatus.PENDING);
        // D12: 防御性设置创建时间
        if (repairOrder.getCreatedAt() == null) {
            repairOrder.setCreatedAt(LocalDateTime.now());
        }

        // D10: 工单编号生成 + 重试机制（3次随机重试，捕获唯一键冲突）
        String date = DateTimeFormatter.ofPattern("yyyyMMdd").format(LocalDate.now());
        for (int i = 0; i < ORDER_NO_MAX_RETRIES; i++) {
            // D11: 100000 + nextInt(900000) 确保 6 位数字不以 0 开头
            long random = 100000 + ThreadLocalRandom.current().nextInt(900000);
            repairOrder.setOrderNo(String.format("R%s%06d", date, random));
            try {
                save(repairOrder);
                return repairOrder;
            } catch (DuplicateKeyException e) {
                // 编号冲突，重置 ID 重试
                repairOrder.setId(null);
                if (i == ORDER_NO_MAX_RETRIES - 1) {
                    throw new RuntimeException("工单创建失败，无法生成唯一编号，请重试", e);
                }
            }
        }
        throw new RuntimeException("工单创建失败，无法生成唯一编号，请重试");
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // RS2: 状态更新事务保护
    public boolean updateStatus(Long id, RepairOrderStatus status, Long handlerId,
                                String rejectReason, String handleResult) {
        // D16: 参数非空校验
        if (id == null) {
            throw new IllegalArgumentException("工单ID不能为空");
        }
        if (status == null) {
            throw new IllegalArgumentException("工单状态不能为空");
        }

        RepairOrder order = getById(id);
        if (order == null) {
            return false;
        }

        // D17: 状态机合法性校验
        RepairOrderStatus currentStatus = order.getStatus();
        RepairOrderStatus[] allowedTargets = VALID_TRANSITIONS.get(currentStatus);
        if (allowedTargets == null) {
            throw new IllegalStateException("未知的当前状态: " + currentStatus);
        }
        boolean validTransition = false;
        for (RepairOrderStatus allowed : allowedTargets) {
            if (allowed == status) {
                validTransition = true;
                break;
            }
        }
        if (!validTransition) {
            throw new IllegalStateException(
                    String.format("无效的状态流转: %s → %s", currentStatus.getDesc(), status.getDesc()));
        }

        // 状态相关字段设置
        order.setStatus(status);
        if (status == RepairOrderStatus.PROCESSING) {
            order.setHandleTime(LocalDateTime.now());
            order.setHandlerId(handlerId); // RS5: 在此记录处理人
        }
        if (status == RepairOrderStatus.COMPLETED) {
            order.setCompleteTime(LocalDateTime.now());
        }

        // D14: 按状态条件设置字段，避免无条件覆盖导致数据丢失
        if (status == RepairOrderStatus.REJECTED) {
            order.setRejectReason(rejectReason != null ? rejectReason : "未填写驳回原因");
        }
        if (status == RepairOrderStatus.COMPLETED || status == RepairOrderStatus.PROCESSING) {
            order.setHandleResult(handleResult);
        }

        return updateById(order);
    }
}