package com.campus.config;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {

    private static final Logger log = LoggerFactory.getLogger(JwtConfig.class);

    /** JWT签名密钥，至少32字节（256位） */
    private final String secret;

    /** Token过期时间（秒） */
    private final Long expiration;

    /**
     * Spring Boot 3.x 自动使用构造函数绑定（无需 @ConstructorBinding）
     * 构造函数在校验通过后才会创建 Bean，确保配置不合法时启动失败
     */
    public JwtConfig(String secret, Long expiration) {
        this.secret = secret;
        this.expiration = expiration;
        if (secret == null || secret.length() < 32) {
            log.error("JWT密钥长度不足32字节，当前长度={}，请检查 JWT_SECRET 环境变量配置",
                    secret != null ? secret.length() : 0);
            throw new IllegalStateException("JWT密钥长度必须至少32字节（256位）");
        }
        if (expiration == null || expiration <= 0) {
            log.error("JWT过期时间不合法，当前值={}，请检查 jwt.expiration 配置", expiration);
            throw new IllegalStateException("JWT过期时间必须大于0");
        }
        log.info("JWT配置校验通过：密钥长度={}字节，过期时间={}秒", secret.length(), expiration);
    }
}