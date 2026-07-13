package com.campus.service;

import com.campus.config.QWeatherProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * 和风天气 API 客户端
 * 通过 QWeatherProperties 自动读取配置，封装 HTTP 请求
 */
@Component
@SuppressWarnings("null")
public class QWeatherClient {

    private static final Logger log = LoggerFactory.getLogger(QWeatherClient.class);

    private final RestTemplate restTemplate;
    private final QWeatherProperties properties;
    private final ObjectMapper objectMapper;

    public QWeatherClient(QWeatherProperties properties) {
        this.properties = properties;
        this.restTemplate = new RestTemplate();
        // 替换默认错误处理器：不抛异常，由 executeGet 统一处理响应
        this.restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                // 不抛异常，由 executeGet 自行判断 code
            }
        });
        // 移除默认 String converter，在首位添加 UTF-8 编码版本，避免中文乱码
        this.restTemplate.getMessageConverters().removeIf(
                c -> c instanceof StringHttpMessageConverter);
        this.restTemplate.getMessageConverters()
                .add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 获取实时天气
     * @param location 城市 LocationID（如 101010100）或经纬度（如 116.41,39.92）
     */
    public Map<String, Object> getWeatherNow(String location) {
        String url = buildUrl(properties.getHost(), properties.getApi().getWeatherNow(), location);
        return executeGet(url);
    }

    /**
     * 获取3天天气预报
     * @param location 城市 LocationID
     */
    public Map<String, Object> getWeather3d(String location) {
        String url = buildUrl(properties.getHost(), properties.getApi().getWeather3d(), location);
        return executeGet(url);
    }

    /**
     * 城市搜索（模糊查询）
     * @param cityName 城市名称，如"广州"、"beijing"
     */
    public Map<String, Object> lookupCity(String cityName) {
        String url = buildUrl(properties.getGeoHost(), properties.getApi().getCityLookup(), cityName);
        return executeGet(url);
    }

    /**
     * 统一构建请求 URL（API 密钥方式：通过 &key= 查询参数认证）
     */
    private String buildUrl(String host, String path, String location) {
        return host + path + "?location=" + encode(location) + "&key=" + properties.getKey();
    }

    /**
     * 执行 GET 请求并解析响应，支持 gzip 解压
     * 由于设置了自定义 ErrorHandler，4xx/5xx 也能拿到响应体
     */
    private Map<String, Object> executeGet(String url) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept-Encoding", "gzip, deflate");
            HttpEntity<?> entity = new HttpEntity<>(headers);

            ResponseEntity<byte[]> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, byte[].class);

            HttpStatusCode statusCode = response.getStatusCode();
            byte[] body = response.getBody();
            String bodyText = (body != null && body.length > 0) ? decompressIfGzip(body) : "(empty)";

            // 4xx/5xx 时打印完整响应便于排查
            if (statusCode.isError()) {
                log.error("QWeather API HTTP {}: url={}, body={}", statusCode.value(), url, bodyText);
                return Collections.singletonMap("error",
                        Map.of("status", statusCode.value(), "detail", bodyText));
            }

            if (body == null) {
                log.warn("QWeather API returned empty body: url={}", url);
                return Collections.emptyMap();
            }

            JsonNode root = objectMapper.readTree(bodyText);

            // 检查和风天气业务错误响应: {"code": "403", "error": {...}}
            if (root.has("error")) {
                JsonNode error = root.get("error");
                log.warn("QWeather API biz error: status={}, title={}, detail={}",
                        error.path("status").asText(),
                        error.path("title").asText(),
                        error.path("detail").asText());
                return Collections.singletonMap("error", error);
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> result = objectMapper.convertValue(root, Map.class);
            return result;
        } catch (Exception e) {
            log.error("QWeather API request failed: url={}, error={}", url, e.getMessage());
            throw new RuntimeException("天气服务请求失败: " + e.getMessage(), e);
        }
    }

    /**
     * 检测并解压 gzip 数据
     * 和风天气 API 可能返回 gzip 压缩响应（对应 curl --compressed）
     */
    private String decompressIfGzip(byte[] data) {
        // GZIP 魔数: 0x1F 0x8B
        if (data.length >= 2 && data[0] == (byte) 0x1F && data[1] == (byte) 0x8B) {
            try (GZIPInputStream gzipIn = new GZIPInputStream(new ByteArrayInputStream(data));
                 ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[4096];
                int n;
                while ((n = gzipIn.read(buffer)) > 0) {
                    out.write(buffer, 0, n);
                }
                return out.toString(StandardCharsets.UTF_8);
            } catch (Exception e) {
                log.warn("GZIP decompression failed, falling back to raw bytes: {}", e.getMessage());
            }
        }
        // 非 gzip 数据，直接按 UTF-8 解码
        return new String(data, StandardCharsets.UTF_8);
    }

    /**
     * 简单 URL 编码（处理中文城市名）
     */
    private String encode(String value) {
        try {
            return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return value;
        }
    }
}
