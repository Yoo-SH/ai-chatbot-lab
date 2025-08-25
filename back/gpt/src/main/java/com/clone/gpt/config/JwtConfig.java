package com.clone.gpt.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {
    
    @Value("${jwt.secret:mySecretKey123456789012345678901234567890}")
    private String secret;
    
    @Value("${jwt.expiration:86400000}") // 24시간
    private Long expiration;
    
    @Value("${jwt.refresh-expiration:604800000}") // 7일
    private Long refreshExpiration;
    
    public String getSecret() {
        return secret;
    }
    
    public Long getExpiration() {
        return expiration;
    }
    
    public Long getRefreshExpiration() {
        return refreshExpiration;
    }
} 