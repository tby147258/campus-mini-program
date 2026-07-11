package com.campus.controller;

import com.campus.annotation.NoAuth;
import com.campus.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    private static final Logger log = LoggerFactory.getLogger(WeatherController.class);

    @Value("${weather.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 获取实时天气（无需登录）
     * 注：apiKey 通过 URL query 参数传递，这是和风天气 API 要求的 GET 方式；
     * 生产环境建议通过服务端代理转发避免 key 明文出现在中间日志中
     */
    @GetMapping("/now")
    @NoAuth
    public Result<?> getNowWeather(@RequestParam String location) {
        // 城市/location 基本格式校验
        if (location.isBlank() || location.length() > 50) {
            return Result.error(400, "城市参数不合法");
        }
        // 防止 CRLF 注入
        if (location.contains("\r") || location.contains("\n")) {
            return Result.error(400, "城市参数包含非法字符");
        }

        String url = "https://devapi.qweather.com/v7/weather/now?location=" + location + "&key=" + apiKey;
        try {
            Map<?, ?> result = restTemplate.getForObject(url, Map.class);
            return Result.success(result);
        } catch (RestClientException e) {
            log.error("和风天气API调用失败: location={}, error={}", location, e.getMessage());
            return Result.error(500, "天气查询失败");
        }
    }
}