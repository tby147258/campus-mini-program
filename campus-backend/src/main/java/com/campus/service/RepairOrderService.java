package com.campus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.entity.RepairOrder;
import com.campus.enums.RepairOrderStatus;

public interface RepairOrderService extends IService<RepairOrder> {

    /**
     * 创建报修工单
     * @param repairOrder 工单信息（不含 userId/status/orderNo）
     * @param userId 当前登录用户 ID
     * @return 创建后的工单
     */
    RepairOrder createOrder(RepairOrder repairOrder, Long userId);

    /**
     * 更新工单状态（含处理人、时间戳记录）
     * @param id 工单 ID
     * @param status 新状态
     * @param handlerId 处理人 ID（可为 null）
     * @param rejectReason 驳回原因（可为 null）
     * @param handleResult 处理结果（可为 null）
     * @return 是否成功
     */
    boolean updateStatus(Long id, RepairOrderStatus status, Long handlerId,
                         String rejectReason, String handleResult);
}