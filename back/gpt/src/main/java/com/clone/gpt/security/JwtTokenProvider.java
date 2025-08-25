package com.clone.gpt.security;

import com.clone.gpt.config.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
    
    private final JwtConfig jwtConfig;
    
    // JWT 전용 RedisTemplate 주입
    @Qualifier("jwtRedisTemplate")
    private final RedisTemplate<String, String> jwtRedisTemplate;
    
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes());
    }
    
    public String generateAccessToken(Long userId, String email) {
        log.info("Access Token 생성 시작: userId={}, email={}", userId, email);
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + jwtConfig.getExpiration());
        
        String token = Jwts.builder()
                .setSubject(userId.toString())
                .claim("email", email)
                .claim("type", "access")
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(getSigningKey())
                .compact();
                
        try {
            // Redis 연결 상태 확인
            log.info("Redis 연결 상태 확인 시작...");
            
            // Redis에 토큰 저장 (블랙리스트 관리용)
            String key = "token:access:" + userId;
            long ttlMillis = jwtConfig.getExpiration();
            
            log.info("Redis 저장 시도: key={}, ttl={}ms", key, ttlMillis);
            
            jwtRedisTemplate.opsForValue().set(
                key, 
                token, 
                ttlMillis, 
                TimeUnit.MILLISECONDS
            );
            
            // 저장 후 즉시 확인
            String savedToken = jwtRedisTemplate.opsForValue().get(key);
            Long ttl = jwtRedisTemplate.getExpire(key, TimeUnit.SECONDS);
            
            log.info("Access Token Redis 저장 성공: key={}, userId={}, exists={}, ttl={}초", 
                    key, userId, savedToken != null, ttl);
                    
        } catch (Exception e) {
            log.error("Access Token Redis 저장 실패: userId={}, error={}", userId, e.getMessage(), e);
        }
        
        return token;
    }
    
    public String generateRefreshToken(Long userId) {
        log.info("Refresh Token 생성 시작: userId={}", userId);
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + jwtConfig.getRefreshExpiration());
        
        String token = Jwts.builder()
                .setSubject(userId.toString())
                .claim("type", "refresh")
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(getSigningKey())
                .compact();
                
        try {
            // Redis 연결 상태 확인 시작
            log.info("Redis 연결 상태 확인 시작...");

            // Redis에 리프레시 토큰 저장
            String key = "token:refresh:" + userId;
            long ttlMillis = jwtConfig.getRefreshExpiration();
            
            log.info("Redis 저장 시도: key={}, ttl={}ms", key, ttlMillis);
            
            jwtRedisTemplate.opsForValue().set(
                key, 
                token, 
                ttlMillis,   
                TimeUnit.MILLISECONDS
            );
            
            // 저장 후 즉시 확인
            String savedToken = jwtRedisTemplate.opsForValue().get(key);
            Long ttl = jwtRedisTemplate.getExpire(key, TimeUnit.SECONDS);
            
            log.info("Refresh Token Redis 저장 성공: key={}, userId={}, exists={}, ttl={}초", 
                    key, userId, savedToken != null, ttl);
                    
        } catch (Exception e) {
            log.error("Refresh Token Redis 저장 실패: userId={}, error={}", userId, e.getMessage(), e);
        }
        
        return token;
    }
    
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return Long.parseLong(claims.getSubject());
    }
    
    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get("email", String.class);
    }
    
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }
    
    public boolean isTokenInRedis(String token, Long userId, String type) {
        String redisToken = (String) jwtRedisTemplate.opsForValue().get("token:" + type + ":" + userId);
        return token.equals(redisToken);
    }
    
    public void invalidateToken(Long userId, String type) {
        jwtRedisTemplate.delete("token:" + type + ":" + userId);
    }
    
    public void invalidateAllTokens(Long userId) {
        jwtRedisTemplate.delete("token:access:" + userId);
        jwtRedisTemplate.delete("token:refresh:" + userId);
    }
    
    public JwtConfig getJwtConfig() {
        return jwtConfig;
    }
} 