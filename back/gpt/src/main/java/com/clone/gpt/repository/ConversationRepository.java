package com.clone.gpt.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.clone.gpt.model.entity.Conversation;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    
    @Query("SELECT c FROM Conversation c WHERE c.user.id = :userId ORDER BY c.updatedAt DESC")
    List<Conversation> findByUserIdOrderByUpdatedAtDesc(@Param("userId") Long userId);
    
    @Query("SELECT c FROM Conversation c WHERE c.user.id = :userId ORDER BY c.updatedAt DESC")
    Page<Conversation> findByUserIdOrderByUpdatedAtDesc(@Param("userId") Long userId, Pageable pageable);
    
    Optional<Conversation> findByIdAndUserId(Long id, Long userId);
    
    void deleteByIdAndUserId(Long id, Long userId);
} 