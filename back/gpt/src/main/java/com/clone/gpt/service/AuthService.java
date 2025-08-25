package com.clone.gpt.service;

import com.clone.gpt.model.dto.request.auth.AuthRequest;
import com.clone.gpt.model.dto.response.auth.AuthResponse;
import com.clone.gpt.model.dto.response.users.UserResponse;
import com.clone.gpt.model.entity.User;
import com.clone.gpt.repository.UserRepository;
import com.clone.gpt.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    
    public AuthResponse register(AuthRequest request) {
        // 이메일 중복 확인
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("이미 존재하는 이메일입니다.");
        }
        
        // 사용자 생성
        User user = User.builder()
                .name(extractNameFromEmail(request.getEmail()))
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();
        
        User savedUser = userRepository.save(user);
        
        // JWT 토큰 생성
        String accessToken = jwtTokenProvider.generateAccessToken(savedUser.getId(), savedUser.getEmail());
        String refreshToken = jwtTokenProvider.generateRefreshToken(savedUser.getId());
        
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtTokenProvider.getJwtConfig().getExpiration())
                .user(convertToUserResponse(savedUser))
                .build();
    }
    
    public AuthResponse login(AuthRequest request) {
        // 사용자 조회
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        // 비밀번호 확인
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }
        
        // JWT 토큰 생성
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());
        
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtTokenProvider.getJwtConfig().getExpiration())
                .user(convertToUserResponse(user))
                .build();
    }
    
    public void logout(Long userId) {
        jwtTokenProvider.invalidateAllTokens(userId);
    }
    
    public AuthResponse refreshToken(String refreshToken) {
        // 토큰 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("유효하지 않은 리프레시 토큰입니다.");
        }
        
        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        
        // Redis에서 토큰 확인
        if (!jwtTokenProvider.isTokenInRedis(refreshToken, userId, "refresh")) {
            throw new RuntimeException("만료된 리프레시 토큰입니다.");
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        // 새로운 토큰 생성
        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getId());
        
        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .expiresIn(jwtTokenProvider.getJwtConfig().getExpiration())
                .user(convertToUserResponse(user))
                .build();
    }
    
    private String extractNameFromEmail(String email) {
        return email.split("@")[0];
    }
    
    private UserResponse convertToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .apiKey(user.getApiKey())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
} 