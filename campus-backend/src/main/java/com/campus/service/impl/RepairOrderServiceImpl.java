package com.campus.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.entity.RepairOrder;
import com.campus.mapper.RepairOrderMapper;
import com.campus.service.RepairOrderService;
import org.springframework.stereotype.Service;

@Service
public class RepairOrderServiceImpl extends ServiceImpl<RepairOrderMapper, RepairOrder> implements RepairOrderService {
}