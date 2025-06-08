package com.clone.gpt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.clone.gpt.model.entity.Message;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId ORDER BY m.createdAt ASC")
    List<Message> findByConversationIdOrderByCreatedAtAsc(@Param("conversationId") Long conversationId);
    
    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId ORDER BY m.createdAt DESC")
    List<Message> findByConversationIdOrderByCreatedAtDesc(@Param("conversationId") Long conversationId);
    
    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId AND m.conversation.user.id = :userId ORDER BY m.createdAt ASC")
    List<Message> findByConversationIdAndUserIdOrderByCreatedAtAsc(@Param("conversationId") Long conversationId, @Param("userId") Long userId);
} 