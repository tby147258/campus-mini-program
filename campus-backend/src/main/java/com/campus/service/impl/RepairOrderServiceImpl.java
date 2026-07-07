package com.campus.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.entity.RepairOrder;
import com.campus.enums.RepairOrderStatus;
import com.campus.mapper.RepairOrderMapper;
import com.campus.service.RepairOrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class RepairOrderServiceImpl extends ServiceImpl<RepairOrderMapper, RepairOrder> implements RepairOrderService {

    @Override
    @Transactional(rollbackFor = Exception.class) // RS4: 工单创建事务保护
    public RepairOrder createOrder(RepairOrder repairOrder, Long userId) {
        repairOrder.setUserId(userId);
        repairOrder.setStatus(RepairOrderStatus.PENDING);

        // 生成工单编号 R + yyyyMMdd + 6位随机数
        String date = DateTimeFormatter.ofPattern("yyyyMMdd").format(LocalDate.now());
        long random = ThreadLocalRandom.current().nextInt(1000000);
        repairOrder.setOrderNo(String.format("R%s%06d", date, random));

        save(repairOrder);
        return repairOrder;
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // RS2: 状态更新事务保护
    public boolean updateStatus(Long id, RepairOrderStatus status, Long handlerId,
                                String rejectReason, String handleResult) {
        // RS3: 非空校验
        if (status == null) {
            throw new IllegalArgumentException("工单状态不能为空");
        }
        RepairOrder order = getById(id);
        if (order == null) {
            return false;
        }
        order.setStatus(status);
        if (status == RepairOrderStatus.PROCESSING) {
            order.setHandleTime(LocalDateTime.now());
            order.setHandlerId(handlerId); // RS5: 在此记录处理人
        }
        if (status == RepairOrderStatus.COMPLETED) {
            order.setCompleteTime(LocalDateTime.now());
        }
        order.setRejectReason(rejectReason);
        order.setHandleResult(handleResult);
        return updateById(order);
    }
}