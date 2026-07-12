package com.campus.controller;

import com.campus.annotation.NoAuth;
import com.campus.common.Result;
import com.campus.service.QWeatherClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 天气控制器（无需登录）
 * 通过 QWeatherClient 自动获取和风天气数据
 */
@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    private static final Logger log = LoggerFactory.getLogger(WeatherController.class);

    private final QWeatherClient qWeatherClient;

    public WeatherController(QWeatherClient qWeatherClient) {
        this.qWeatherClient = qWeatherClient;
    }

    /**
     * 获取实时天气
     * @param location 城市 LocationID（如 101010100）或经纬度（如 116.41,39.92）
     * @param city     可选城市名称，用于前端展示
     */
    @GetMapping("/now")
    @NoAuth
    public Result<?> getNowWeather(@RequestParam String location,
                                   @RequestParam(required = false) String city) {
        if (!isValidLocation(location)) {
            return Result.error(400, "城市参数不合法");
        }
        return callWithErrorHandling(() -> {
            Map<String, Object> result = qWeatherClient.getWeatherNow(location);
            injectCity(result, city, location);
            return result;
        }, location);
    }

    /**
     * 获取3天天气预报
     * @param location 城市 LocationID
     * @param city     可选城市名称
     */
    @GetMapping("/3d")
    @NoAuth
    public Result<?> getWeather3d(@RequestParam String location,
                                  @RequestParam(required = false) String city) {
        if (!isValidLocation(location)) {
            return Result.error(400, "城市参数不合法");
        }
        return callWithErrorHandling(() -> {
            Map<String, Object> result = qWeatherClient.getWeather3d(location);
            injectCity(result, city, location);
            return result;
        }, location);
    }

    /**
     * 城市搜索（支持模糊查询）
     * @param name 城市名称，如"广州"、"beijing"
     */
    @GetMapping("/city")
    @NoAuth
    public Result<?> lookupCity(@RequestParam String name) {
        if (name == null || name.isBlank() || name.length() > 50) {
            return Result.error(400, "城市名称不合法");
        }
        if (name.contains("\r") || name.contains("\n")) {
            return Result.error(400, "城市参数包含非法字符");
        }
        return callWithErrorHandling(() -> qWeatherClient.lookupCity(name), name);
    }

    /**
     * 参数校验
     */
    private boolean isValidLocation(String location) {
        if (location == null || location.isBlank() || location.length() > 50) {
            return false;
        }
        if (location.contains("\r") || location.contains("\n")) {
            return false;
        }
        return true;
    }

    /**
     * 将城市名称注入到 now 对象中（供前端展示使用）
     * 和风天气 API 的 now 对象不包含城市名称，需手动补充
     */
    @SuppressWarnings("unchecked")
    private void injectCity(Map<String, Object> result, String city, String location) {
        if (city != null && !city.isBlank() && result.get("now") instanceof Map) {
            ((Map<String, Object>) result.get("now")).put("city", city);
        } else if (result.get("now") instanceof Map) {
            // 如果未传 city，用 location 作为兜底
            ((Map<String, Object>) result.get("now")).put("city", location);
        }
    }

    /**
     * 统一错误处理
     */
    private Result<?> callWithErrorHandling(java.util.function.Supplier<Map<String, Object>> call, String param) {
        try {
            Map<String, Object> result = call.get();
            if (result.containsKey("error")) {
                log.warn("和风天气API返回错误: param={}, error={}", param, result.get("error"));
                return Result.error(500, "天气服务未授权，请前往 console.qweather.com 创建项目并订阅API");
            }
            return Result.success(result);
        } catch (Exception e) {
            log.error("天气服务调用失败: param={}, error={}", param, e.getMessage());
            return Result.error(500, "天气查询失败，请检查网络或API配置");
        }
    }
}
