from typing import Dict, List, Any, Optional
from langchain.memory import (
    ConversationBufferWindowMemory,
    ConversationSummaryBufferMemory,
    ConversationTokenBufferMemory
)
from langchain.schema import BaseMessage, HumanMessage, AIMessage, SystemMessage


class ChatMemoryService:
    """CLOVAX API를 위한 멀티턴 대화 메모리 서비스"""
    
    def __init__(self):
        # 세션별 메모리를 저장하는 딕셔너리
        self._session_memories: Dict[str, ConversationBufferWindowMemory] = {}
        
    def get_or_create_memory(
        self, 
        session_id: str, 
        memory_type: str = "buffer_window",
        k: int = 10
    ) -> ConversationBufferWindowMemory:
        """세션 ID에 따른 메모리 인스턴스 생성 또는 반환"""
        if session_id not in self._session_memories:
            if memory_type == "buffer_window":
                # 최근 K개의 대화만 저장
                memory = ConversationBufferWindowMemory(
                    k=k,
                    return_messages=True,
                    memory_key="chat_history"
                )
            elif memory_type == "token_buffer":
                # 토큰 수 기준으로 대화 저장
                memory = ConversationTokenBufferMemory(
                    max_token_limit=2000,
                    return_messages=True,
                    memory_key="chat_history"
                )
            else:
                # 기본값: buffer_window
                memory = ConversationBufferWindowMemory(
                    k=k,
                    return_messages=True,
                    memory_key="chat_history"
                )
            
            self._session_memories[session_id] = memory
            
        return self._session_memories[session_id]
    
    def add_message_to_memory(
        self, 
        session_id: str, 
        user_message: str, 
        assistant_message: str,
        memory_type: str = "buffer_window"
    ):
        """대화 내용을 메모리에 추가"""
        memory = self.get_or_create_memory(session_id, memory_type)
        
        # langchain 메모리에 대화 내용 저장
        memory.chat_memory.add_user_message(user_message)
        memory.chat_memory.add_ai_message(assistant_message)
    
    def get_messages_for_clovax(
        self, 
        session_id: str, 
        current_messages: List[Dict[str, Any]],
        memory_type: str = "buffer_window"
    ) -> List[Dict[str, Any]]:
        """CLOVAX API 형식에 맞는 메시지 배열 생성 (이전 대화 포함)"""
        memory = self.get_or_create_memory(session_id, memory_type)
        
        # 메모리에서 이전 대화 내역 가져오기
        chat_history = memory.chat_memory.messages
        
        # CLOVAX API 형식으로 변환
        clovax_messages = []
        
        # system 메시지 먼저 추가 (현재 메시지에서 추출)
        system_messages = [msg for msg in current_messages if msg.get("role") == "system"]
        clovax_messages.extend(system_messages)
        
        # 이전 대화 내역을 CLOVAX 형식으로 변환하여 추가
        for msg in chat_history:
            if isinstance(msg, HumanMessage):
                clovax_messages.append({
                    "role": "user",
                    "content": msg.content
                })
            elif isinstance(msg, AIMessage):
                clovax_messages.append({
                    "role": "assistant", 
                    "content": msg.content
                })
        
        # 현재 사용자 메시지 추가 (system 제외)
        current_user_messages = [
            msg for msg in current_messages 
            if msg.get("role") in ["user", "assistant"] and msg.get("role") != "system"
        ]
        clovax_messages.extend(current_user_messages)
        
        return clovax_messages
    
    def clear_session_memory(self, session_id: str):
        """특정 세션의 메모리 삭제"""
        if session_id in self._session_memories:
            del self._session_memories[session_id]
    
    def get_memory_stats(self, session_id: str) -> Dict[str, Any]:
        """메모리 상태 정보 반환"""
        if session_id not in self._session_memories:
            return {"message_count": 0, "exists": False}
        
        memory = self._session_memories[session_id]
        return {
            "message_count": len(memory.chat_memory.messages),
            "exists": True,
            "memory_type": type(memory).__name__
        }


# 싱글턴 인스턴스
chat_memory_service = ChatMemoryService()