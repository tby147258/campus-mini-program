package com.campus.controller;

import com.campus.annotation.NoAuth;
import com.campus.common.Result;
import com.campus.service.CaptchaService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/captcha")
public class CaptchaController {

    private final CaptchaService captchaService;

    public CaptchaController(CaptchaService captchaService) {
        this.captchaService = captchaService;
    }

    /**
     * 获取滑块验证码（无需登录）
     */
    @GetMapping
    @NoAuth
    public Result<?> getCaptcha() {
        return captchaService.generate();
    }

    /**
     * 验证滑块验证码
     */
    @PostMapping("/verify")
    @NoAuth
    public Result<?> verifyCaptcha(@RequestBody Map<String, Object> params) {
        String token = (String) params.get("token");
        int position = ((Number) params.get("position")).intValue();
        return captchaService.verify(token, position);
    }
}