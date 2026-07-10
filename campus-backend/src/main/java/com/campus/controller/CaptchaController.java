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
        // D14: 类型安全校验 — 防止 ClassCastException 和 NullPointerException
        Object tokenObj = params.get("token");
        if (!(tokenObj instanceof String token) || token.isBlank()) {
            return Result.error(400, "验证码凭证不能为空");
        }

        Object positionObj = params.get("position");
        if (!(positionObj instanceof Number positionNum)) {
            return Result.error(400, "滑块位置参数缺失或无效");
        }
        int position = positionNum.intValue();

        return captchaService.verify(token, position);
    }
}