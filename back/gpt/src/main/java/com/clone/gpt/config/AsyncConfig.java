package com.clone.gpt.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class AsyncConfig {
    // 기본 ThreadPoolTaskExecutor를 사용
    // 필요시 커스텀 Executor를 빈으로 등록할 수 있음
} 