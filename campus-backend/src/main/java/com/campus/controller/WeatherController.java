package com.campus.controller;

import com.campus.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/api/weather")
public class WeatherController {
    @Value("${weather.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/now")
    public Result<?> getNowWeather(@RequestParam String location) {
        String url = "https://devapi.qweather.com/v7/weather/now?location=" + location + "&key=" + apiKey;
        try {
            Map<?, ?> result = restTemplate.getForObject(url, Map.class);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(500, "天气查询失败");
        }
    }
}