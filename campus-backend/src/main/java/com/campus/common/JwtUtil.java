package com.campus.common;

import com.campus.config.JwtConfig;
import com.campus.enums.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Objects;

@Component
public class JwtUtil {
    private final JwtConfig jwtConfig;
    private final SecretKey key;

    public JwtUtil(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
        this.key = Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Long userId, UserRole role) {
        // JK10: 参数非空校验
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(role, "role must not be null");

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("role", role.getCode())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtConfig.getExpiration() * 1000))
                .signWith(key)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long getUserId(String token) {
        return Long.parseLong(parseToken(token).getSubject());
    }

    public Integer getRole(String token) {
        return (Integer) parseToken(token).get("role");
    }

    public UserRole getRoleEnum(String token) {
        Integer code = (Integer) parseToken(token).get("role");
        return UserRole.fromCode(code);
    }
}