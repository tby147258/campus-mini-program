package com.campus.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.annotation.RoleRequired;
import com.campus.common.Result;
import com.campus.entity.*;
import com.campus.enums.LostFoundStatus;
import com.campus.enums.RepairOrderStatus;
import com.campus.service.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
@RoleRequired(1)
@SuppressWarnings("null")
public class StatisticsController {

    private final AnnouncementService announcementService;
    private final LostFoundService lostFoundService;
    private final RepairOrderService repairOrderService;
    private final UserService userService;

    public StatisticsController(AnnouncementService announcementService,
                                LostFoundService lostFoundService,
                                RepairOrderService repairOrderService,
                                UserService userService) {
        this.announcementService = announcementService;
        this.lostFoundService = lostFoundService;
        this.repairOrderService = repairOrderService;
        this.userService = userService;
    }

    @GetMapping
    public Result<?> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // 用户统计
        stats.put("userCount", userService.count());
        stats.put("adminCount", userService.lambdaQuery().eq(User::getRole, 1).count());
        stats.put("studentCount", userService.lambdaQuery().eq(User::getRole, 0).count());

        // 公告统计
        stats.put("announcementCount", announcementService.count());

        // 失物招领统计
        stats.put("lostFoundCount", lostFoundService.count());
        stats.put("lostFoundPending", lostFoundService.lambdaQuery()
                .eq(LostFound::getStatus, LostFoundStatus.PENDING_AUDIT).count());
        stats.put("lostFoundPublished", lostFoundService.lambdaQuery()
                .eq(LostFound::getStatus, LostFoundStatus.PUBLISHED).count());
        stats.put("lostFoundRejected", lostFoundService.lambdaQuery()
                .eq(LostFound::getStatus, LostFoundStatus.REJECTED).count());
        stats.put("lostFoundClosed", lostFoundService.lambdaQuery()
                .eq(LostFound::getStatus, LostFoundStatus.CLOSED).count());

        // 报修工单统计
        stats.put("repairOrderCount", repairOrderService.count());
        stats.put("repairPending", repairOrderService.lambdaQuery()
                .eq(RepairOrder::getStatus, RepairOrderStatus.PENDING).count());
        stats.put("repairProcessing", repairOrderService.lambdaQuery()
                .eq(RepairOrder::getStatus, RepairOrderStatus.PROCESSING).count());
        stats.put("repairCompleted", repairOrderService.lambdaQuery()
                .eq(RepairOrder::getStatus, RepairOrderStatus.COMPLETED).count());
        stats.put("repairRejected", repairOrderService.lambdaQuery()
                .eq(RepairOrder::getStatus, RepairOrderStatus.REJECTED).count());

        return Result.success(stats);
    }
}