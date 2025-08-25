package com.clone.gpt.service;

import com.clone.gpt.model.dto.request.prompt.PromptRequest;
import com.clone.gpt.model.dto.response.prompt.PromptResponse;
import com.clone.gpt.model.entity.Prompt;
import com.clone.gpt.model.entity.User;
import com.clone.gpt.repository.PromptRepository;
import com.clone.gpt.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PromptService {
    
    private final PromptRepository promptRepository;
    private final UserRepository userRepository;
    
    public PromptResponse createPrompt(Long userId, PromptRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        Prompt prompt = Prompt.builder()
                .user(user)
                .title(request.getTitle())
                .content(request.getContent())
                .build();
        
        Prompt savedPrompt = promptRepository.save(prompt);
        
        return convertToPromptResponse(savedPrompt);
    }
    
    @Transactional(readOnly = true)
    public List<PromptResponse> getUserPrompts(Long userId) {
        List<Prompt> prompts = promptRepository.findByUserIdOrderByCreatedAtDesc(userId);
        
        return prompts.stream()
                .map(this::convertToPromptResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public PromptResponse getPrompt(Long userId, Long promptId) {
        Prompt prompt = promptRepository.findByIdAndUserId(promptId, userId)
                .orElseThrow(() -> new RuntimeException("프롬프트를 찾을 수 없습니다."));
        
        return convertToPromptResponse(prompt);
    }
    
    public PromptResponse updatePrompt(Long userId, Long promptId, PromptRequest request) {
        Prompt prompt = promptRepository.findByIdAndUserId(promptId, userId)
                .orElseThrow(() -> new RuntimeException("프롬프트를 찾을 수 없습니다."));
        
        prompt.setTitle(request.getTitle());
        prompt.setContent(request.getContent());
        Prompt updatedPrompt = promptRepository.save(prompt);
        
        return convertToPromptResponse(updatedPrompt);
    }
    
    public void deletePrompt(Long userId, Long promptId) {
        Prompt prompt = promptRepository.findByIdAndUserId(promptId, userId)
                .orElseThrow(() -> new RuntimeException("프롬프트를 찾을 수 없습니다."));
        
        promptRepository.delete(prompt);
    }
    
    private PromptResponse convertToPromptResponse(Prompt prompt) {
        return PromptResponse.builder()
                .id(prompt.getId())
                .title(prompt.getTitle())
                .content(prompt.getContent())
                .createdAt(prompt.getCreatedAt())
                .updatedAt(prompt.getUpdatedAt())
                .build();
    }
} 