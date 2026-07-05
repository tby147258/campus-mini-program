package com.campus.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.entity.LostFound;
import com.campus.mapper.LostFoundMapper;
import com.campus.service.LostFoundService;
import org.springframework.stereotype.Service;

@Service
public class LostFoundServiceImpl extends ServiceImpl<LostFoundMapper, LostFound> implements LostFoundService {
}