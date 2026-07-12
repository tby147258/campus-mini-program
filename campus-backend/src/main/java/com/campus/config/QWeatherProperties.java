package com.campus.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 和风天气 API 配置
 * 绑定 application.yml 中 qweather.* 配置项
 */
@Data
@Component
@ConfigurationProperties(prefix = "qweather")
public class QWeatherProperties {

    /** API Key */
    private String key;

    /** 天气 API Host（免费订阅: devapi.qweather.com，付费订阅: api.qweather.com） */
    private String host;

    /** 城市搜索 API Host */
    private String geoHost;

    /** API 路径 */
    private Api api = new Api();

    @Data
    public static class Api {
        /** 实时天气 */
        private String weatherNow = "/v7/weather/now";
        /** 3天预报 */
        private String weather3d = "/v7/weather/3d";
        /** 城市搜索 */
        private String cityLookup = "/v2/city/lookup";
    }
}
