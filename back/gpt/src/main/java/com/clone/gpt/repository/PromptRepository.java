package com.clone.gpt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.clone.gpt.model.entity.Prompt;

import java.util.List;
import java.util.Optional;

@Repository
public interface PromptRepository extends JpaRepository<Prompt, Long> {
    
    @Query("SELECT p FROM Prompt p WHERE p.user.id = :userId ORDER BY p.createdAt DESC")
    List<Prompt> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);
    
    Optional<Prompt> findByIdAndUserId(Long id, Long userId);
    
    void deleteByIdAndUserId(Long id, Long userId);
} 