import React, { useState, useEffect } from 'react';
import './Sidebar.css';

const Sidebar = ({ onNewChat, onSelectChat, selectedChatId, onClearEmptyConversation, onRefreshHistory }) => {
  const [chatHistory, setChatHistory] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  // 메시지가 없는 빈 채팅 ID를 추적
  const [emptyConversationId, setEmptyConversationId] = useState(null);
  // 삭제 중인 채팅 ID들을 추적
  const [deletingChats, setDeletingChats] = useState(new Set());

  // API Base URL
  const API_BASE_URL = 'http://localhost:8080/api';

  // 채팅에 메시지가 있는지 확인하는 함수
  const checkConversationHasMessages = async (conversationId) => {
    try {
      const token = localStorage.getItem('accessToken');
      
      const response = await fetch(`${API_BASE_URL}/messages/conversation/${conversationId}`, {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
      });

      if (!response.ok) {
        throw new Error('메시지 확인에 실패했습니다.');
      }

      const messages = await response.json();
      return messages.length > 0;
    } catch (err) {
      console.error('메시지 확인 오류:', err);
      return true; // 오류 발생 시 삭제하지 않음
    }
  };

  // 빈 채팅을 삭제하는 함수
  const deleteEmptyConversation = async (conversationId) => {
    try {
      const token = localStorage.getItem('accessToken');
      
      const response = await fetch(`${API_BASE_URL}/conversations/${conversationId}`, {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
      });

      if (!response.ok) {
        throw new Error('채팅 삭제에 실패했습니다.');
      }

      console.log(`빈 채팅 ${conversationId}이 삭제되었습니다.`);
      
      // 채팅 기록 새로고침
      await fetchChatHistory();
      
      return true;
    } catch (err) {
      console.error('채팅 삭제 오류:', err);
      return false;
    }
  };

  // 이전 빈 채팅이 있다면 삭제하는 함수
  const handleEmptyConversationCleanup = async (newConversationId = null) => {
    if (emptyConversationId && emptyConversationId !== newConversationId) {
      const hasMessages = await checkConversationHasMessages(emptyConversationId);
      if (!hasMessages) {
        await deleteEmptyConversation(emptyConversationId);
      }
      setEmptyConversationId(null);
    }
  };

  // 채팅 기록을 백엔드에서 가져오는 함수
  const fetchChatHistory = async () => {
    try {
      setLoading(true);
      const token = localStorage.getItem('accessToken');
      
      const response = await fetch(`${API_BASE_URL}/conversations`, {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
      });

      if (!response.ok) {
        throw new Error('채팅 기록을 가져오는데 실패했습니다.');
      }

      const conversations = await response.json();
      
      // 시간별로 그룹화
      const groupedChats = conversations.reduce((acc, conversation) => {
        const timeGroup = getTimeGroup(conversation.updatedAt);
        if (!acc[timeGroup]) {
          acc[timeGroup] = [];
        }
        acc[timeGroup].push({
          id: conversation.id,
          title: conversation.title,
          time: timeGroup,
          updatedAt: conversation.updatedAt
        });
        return acc;
      }, {});

      setChatHistory(groupedChats);
      setError(null);
    } catch (err) {
      console.error('채팅 기록 로딩 오류:', err);
      setError(err.message);
      // 오류 발생 시 기본 데이터 사용
      setChatHistory(FALLBACK_CHAT_HISTORY);
    } finally {
      setLoading(false);
    }
  };

  // 시간 그룹을 결정하는 함수
  const getTimeGroup = (updatedAt) => {
    const now = new Date();
    const updateDate = new Date(updatedAt);
    const diffInDays = Math.floor((now - updateDate) / (1000 * 60 * 60 * 24));

    if (diffInDays === 0) {
      return '오늘';
    } else if (diffInDays === 1) {
      return '어제';
    } else if (diffInDays <= 7) {
      return '지난 7일';
    } else if (diffInDays <= 30) {
      return '지난 30일';
    } else {
      return '이전';
    }
  };

  // 새 채팅 생성 함수
  const handleNewChat = async () => {
    try {
      // 이전 빈 채팅 정리
      await handleEmptyConversationCleanup();

      const token = localStorage.getItem('accessToken');
      
      const response = await fetch(`${API_BASE_URL}/conversations`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          title: '새 대화'
        })
      });

      if (!response.ok) {
        throw new Error('새 채팅 생성에 실패했습니다.');
      }

      const newConversation = await response.json();
      
      // 새로 생성된 채팅을 빈 채팅으로 표시
      setEmptyConversationId(newConversation.id);
      
      // 채팅 기록 새로고침
      await fetchChatHistory();
      
      // 새 채팅으로 이동
      if (onNewChat) {
        onNewChat(newConversation.id);
      }
    } catch (err) {
      console.error('새 채팅 생성 오류:', err);
      // 오류 발생 시 기본 새 채팅 처리
      if (onNewChat) {
        onNewChat();
      }
    }
  };

  // 채팅 선택 처리 함수
  const handleSelectChat = async (chatId) => {
    // 다른 채팅 선택 시 이전 빈 채팅 정리
    await handleEmptyConversationCleanup(chatId);
    
    if (onSelectChat) {
      onSelectChat(chatId);
    }
  };

  // 빈 채팅 상태를 해제하는 함수 (Chat 컴포넌트에서 사용)
  const clearEmptyConversation = (conversationId) => {
    if (emptyConversationId === conversationId) {
      setEmptyConversationId(null);
    }
  };

  // Chat 컴포넌트에 빈 채팅 상태 해제 함수 제공
  useEffect(() => {
    if (onClearEmptyConversation) {
      onClearEmptyConversation(clearEmptyConversation);
    }
  }, [onClearEmptyConversation, emptyConversationId]);

  // Chat 컴포넌트에 채팅 기록 새로고침 함수 제공
  useEffect(() => {
    if (onRefreshHistory) {
      onRefreshHistory(fetchChatHistory);
    }
  }, [onRefreshHistory]);

  // 컴포넌트 마운트 시 채팅 기록 로드
  useEffect(() => {
    fetchChatHistory();
  }, []);

  // 채팅 삭제 함수
  const handleDeleteChat = async (chatId, event) => {
    // 이벤트 버블링 방지 (채팅 선택 이벤트와 충돌 방지)
    event.stopPropagation();
    
    // 확인 다이얼로그
    if (!window.confirm('이 대화를 삭제하시겠습니까? 삭제된 대화는 복구할 수 없습니다.')) {
      return;
    }

    try {
      // 삭제 중 상태 표시
      setDeletingChats(prev => new Set([...prev, chatId]));
      
      const token = localStorage.getItem('accessToken');
      
      const response = await fetch(`${API_BASE_URL}/conversations/${chatId}`, {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
      });

      if (!response.ok) {
        throw new Error('채팅 삭제에 실패했습니다.');
      }

      console.log(`채팅 ${chatId}이 삭제되었습니다.`);
      
      // 현재 선택된 채팅이 삭제된 경우 새 채팅으로 이동
      if (selectedChatId === chatId) {
        if (onNewChat) {
          onNewChat(); // 새 채팅 생성
        }
      }
      
      // 채팅 기록 새로고침
      await fetchChatHistory();
      
    } catch (err) {
      console.error('채팅 삭제 오류:', err);
      setError(`채팅 삭제 실패: ${err.message}`);
    } finally {
      // 삭제 중 상태 해제
      setDeletingChats(prev => {
        const newSet = new Set(prev);
        newSet.delete(chatId);
        return newSet;
      });
    }
  };

  return (
    <div className="sidebar">
      <div className="sidebar-header">
        <button className="new-chat-btn" onClick={handleNewChat} disabled={loading}>
          <span className="plus-icon">+</span>
          새 채팅
        </button>
      </div>

      <div className="chat-history">
        {loading ? (
          <div className="loading-message">채팅 기록을 불러오는 중...</div>
        ) : error ? (
          <div className="error-message">
            <p>오류: {error}</p>
            <button onClick={fetchChatHistory} className="retry-btn">다시 시도</button>
          </div>
        ) : (
          Object.entries(chatHistory).map(([timeGroup, chats]) => (
            <div key={timeGroup} className="time-group">
              <h3 className="time-group-title">{timeGroup}</h3>
              {chats.map((chat) => (
                <div 
                  key={chat.id} 
                  className={`chat-item ${selectedChatId === chat.id ? 'selected' : ''} ${deletingChats.has(chat.id) ? 'deleting' : ''}`}
                  onClick={() => handleSelectChat(chat.id)}
                >
                  <span className="chat-title">{chat.title}</span>
                  <button
                    className="delete-chat-btn"
                    onClick={(e) => handleDeleteChat(chat.id, e)}
                    disabled={deletingChats.has(chat.id)}
                    title="대화 삭제"
                  >
                    {deletingChats.has(chat.id) ? '⏳' : '🗑️'}
                  </button>
                </div>
              ))}
            </div>
          ))
        )}
      </div>

      <div className="sidebar-footer">
        <div className="upgrade-section">
          <div className="profile-item">
            <span className="profile-icon">🆙</span>
            <span className="upgrade-text">플랜 업그레이드</span>
          </div>
          <div className="profile-item">
            <span className="profile-icon">❓</span>
            <span className="upgrade-text">최고 모델로 더 많은 액세스</span>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Sidebar; 