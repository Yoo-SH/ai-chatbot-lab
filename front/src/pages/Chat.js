import React, { useState, useRef, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Sidebar from '../components/Sidebar';
import PromptSelector from '../components/PromptSelector';
import ChatService from '../services/ChatService';
import ConversationService from '../services/ConversationService';
import './Chat.css';

const Chat = () => {
  const [messages, setMessages] = useState([]);
  const [inputMessage, setInputMessage] = useState('');
  const [isTyping, setIsTyping] = useState(false);
  const [currentConversationId, setCurrentConversationId] = useState(null);
  const [showPromptSelector, setShowPromptSelector] = useState(false);
  const [error, setError] = useState('');
  const [isStreaming, setIsStreaming] = useState(false);
  const [conversationTitle, setConversationTitle] = useState('새 대화');
  
  const messagesEndRef = useRef(null);
  const textareaRef = useRef(null);
  const navigate = useNavigate();
  
  const chatService = new ChatService();
  const conversationService = new ConversationService();
  let currentStreamControl = useRef(null);
  
  // Sidebar에서 빈 채팅 상태를 해제하는 함수를 저장
  const clearEmptyConversationRef = useRef(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  };

  useEffect(() => {
    // 로그인 체크
    const token = localStorage.getItem('accessToken');
    if (!token) {
      navigate('/login');
      return;
    }

    // 토큰 설정
    chatService.setAccessToken(token);
    conversationService.setAccessToken(token);

    // 새로고침 시 빈 상태로 시작 (자동으로 새 대화 생성하지 않음)
    
    scrollToBottom();
  }, []);

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const handleNewChat = async (conversationId = null) => {
    // 현재 스트리밍 중단
    if (currentStreamControl.current) {
      currentStreamControl.current.stop();
      currentStreamControl.current = null;
    }
    
    setIsStreaming(false);
    setIsTyping(false);
    
    if (conversationId) {
      // 특정 conversation ID가 제공된 경우 해당 대화로 이동
      setCurrentConversationId(conversationId);
      setMessages([]);
      setConversationTitle('새 대화');
      setError('');
    } else {
      // 새 대화 생성
      try {
        const conversation = await conversationService.createConversation('새 대화');
        setCurrentConversationId(conversation.id);
        setConversationTitle(conversation.title);
        setMessages([]);
        setError('');
      } catch (error) {
        console.error('대화 생성 실패:', error);
        setError('새 대화를 생성할 수 없습니다. 다시 시도해주세요.');
      }
    }
  };

  const handleSelectChat = async (conversationId) => {
    try {
      // 현재 스트리밍 중단
      if (currentStreamControl.current) {
        currentStreamControl.current.stop();
        currentStreamControl.current = null;
      }
      
      setIsStreaming(false);
      setIsTyping(false);
      setError('');

      // 선택한 대화의 메시지 로드
      const conversation = await conversationService.getConversation(conversationId);
      
      setCurrentConversationId(conversationId);
      setConversationTitle(conversation.title);
      
      // 메시지 형식 변환
      const formattedMessages = [];
      if (conversation.messages && conversation.messages.length > 0) {
        conversation.messages.forEach(msg => {
          formattedMessages.push({
            id: msg.id,
            text: msg.content,
            sender: msg.role === 'USER' ? 'user' : 'ai',
            timestamp: new Date(msg.createdAt),
            messageId: msg.id
          });
        });
      }
      
      setMessages(formattedMessages);
    } catch (error) {
      console.error('대화 로드 실패:', error);
      setError('대화를 불러올 수 없습니다. 다시 시도해주세요.');
    }
  };

  // 제목 자동 업데이트 함수
  const updateConversationTitle = async (firstMessage) => {
    if (conversationTitle === '새 대화' && firstMessage && firstMessage.length > 0) {
      try {
        const newTitle = firstMessage.length > 50 ? 
          firstMessage.substring(0, 50) + '...' : 
          firstMessage;
        
        await conversationService.updateConversation(currentConversationId, newTitle);
        setConversationTitle(newTitle);
      } catch (error) {
        console.error('제목 업데이트 실패:', error);
      }
    }
  };

  const handleSendMessage = (e) => {
    e.preventDefault();
    if (inputMessage.trim() && !isStreaming) {
      sendMessageToAPI(inputMessage.trim());
      setInputMessage('');
    }
  };

  const sendMessageToAPI = async (message) => {
    let conversationId = currentConversationId;
    
    // 대화 ID가 없는 경우 새 대화 생성
    if (!conversationId) {
      try {
        const conversation = await conversationService.createConversation('새 대화');
        conversationId = conversation.id;
        setCurrentConversationId(conversation.id);
        setConversationTitle(conversation.title);
        
        // 새로 생성된 채팅을 빈 채팅으로 표시 (Sidebar에서 관리)
        // 이 시점에서는 아직 메시지를 보내기 전이므로 빈 상태
      } catch (error) {
        console.error('대화 생성 실패:', error);
        setError('새 대화를 생성할 수 없습니다. 다시 시도해주세요.');
        return;
      }
    }

    // 첫 번째 메시지 전송 시 빈 채팅 상태 해제
    if (messages.length === 0 && clearEmptyConversationRef.current) {
      clearEmptyConversationRef.current(conversationId);
    }

    // 첫 번째 메시지인 경우 제목 업데이트
    if (messages.length === 0) {
      await updateConversationTitle(message);
    }

    // 사용자 메시지 추가
    const userMessage = {
      id: Date.now(),
      text: message,
      sender: "user",
      timestamp: new Date()
    };
    
    setMessages(prev => [...prev, userMessage]);
    setIsTyping(true);
    setIsStreaming(true);
    setError('');

    // AI 응답 메시지 미리 생성
    const aiMessageId = Date.now() + 1;
    const aiMessage = {
      id: aiMessageId,
      text: '',
      sender: "ai",
      timestamp: new Date()
    };
    
    setMessages(prev => [...prev, aiMessage]);

    try {
      // 스트리밍 방식으로 메시지 전송 (생성된 conversationId 사용)
      const streamControl = await chatService.sendStreamingMessage(
        conversationId,
        message,
        null,
        // onChunk - 실시간 응답 업데이트
        (chunk) => {
          if (chunk.type === 'CONTENT' && chunk.content) {
            setMessages(prev => 
              prev.map(msg => 
                msg.id === aiMessageId 
                  ? { ...msg, text: chunk.content }
                  : msg
              )
            );
          }
        },
        // onComplete - 응답 완료
        (result) => {
          setMessages(prev => 
            prev.map(msg => 
              msg.id === aiMessageId 
                ? { ...msg, text: result.content, messageId: result.messageId }
                : msg
            )
          );
          setIsTyping(false);
          setIsStreaming(false);
          currentStreamControl.current = null;
        },
        // onError - 오류 처리
        (error) => {
          console.error('메시지 전송 오류:', error);
          setError('메시지 전송에 실패했습니다: ' + error.message);
          
          // 실패한 AI 메시지 제거
          setMessages(prev => prev.filter(msg => msg.id !== aiMessageId));
          setIsTyping(false);
          setIsStreaming(false);
          currentStreamControl.current = null;
        }
      );

      currentStreamControl.current = streamControl;

    } catch (error) {
      console.error('메시지 전송 실패:', error);
      setError('서버와 연결할 수 없습니다. 네트워크를 확인해주세요.');
      
      // 실패한 AI 메시지 제거
      setMessages(prev => prev.filter(msg => msg.id !== aiMessageId));
      setIsTyping(false);
      setIsStreaming(false);
    }
  };

  const handleKeyPress = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSendMessage(e);
    }
  };

  const adjustTextareaHeight = () => {
    const textarea = textareaRef.current;
    if (textarea) {
      textarea.style.height = 'auto';
      textarea.style.height = Math.min(textarea.scrollHeight, 200) + 'px';
    }
  };

  useEffect(() => {
    adjustTextareaHeight();
  }, [inputMessage]);

  // 컴포넌트 언마운트 시 스트리밍 중단
  useEffect(() => {
    return () => {
      if (currentStreamControl.current) {
        currentStreamControl.current.stop();
      }
    };
  }, []);

  return (
    <div className="chat-layout">
      <Sidebar 
        onNewChat={handleNewChat} 
        onSelectChat={handleSelectChat}
        selectedChatId={currentConversationId}
        onClearEmptyConversation={(clearFunction) => {
          clearEmptyConversationRef.current = clearFunction;
        }}
      />
      {showPromptSelector && (
        <PromptSelector
          onSelect={text => {
            setInputMessage(text);
            setShowPromptSelector(false);
          }}
          onClose={() => setShowPromptSelector(false)}
        />
      )}
      <div className="chat-main">
        {messages.length === 0 ? (
          <div className="empty-state">
            <div className="empty-content">
              <h1 className="welcome-title">무엇을 도와드릴까요?</h1>
              <p className="welcome-subtitle">
                {currentConversationId && conversationTitle !== '새 대화' ? 
                  `"${conversationTitle}"` : 
                  'ChatGPT 클론에 오신 것을 환영합니다! 새로운 대화를 시작해보세요.'}
              </p>
              {error && (
                <div className="error-banner">
                  <span className="error-icon">⚠️</span>
                  {error}
                </div>
              )}
            </div>
          </div>
        ) : (
          <div className="messages-container">
            {error && (
              <div className="error-banner">
                <span className="error-icon">⚠️</span>
                {error}
                <button onClick={() => setError('')} className="error-close">✕</button>
              </div>
            )}
            
            {messages.map((message) => (
              <div key={message.id} className={`message-wrapper ${message.sender}`}>
                <div className="message-content">
                  <div className="message-text">
                    {message.sender === 'ai' && message.text === '' && isTyping ? (
                      <div className="typing-indicator">
                        <div className="typing-dots">
                          <span></span>
                          <span></span>
                          <span></span>
                        </div>
                      </div>
                    ) : (
                      <pre className="message-pre">{message.text}</pre>
                    )}
                  </div>
                </div>
              </div>
            ))}
            
            <div ref={messagesEndRef} />
          </div>
        )}

        <div className="input-section">
          <div className="input-container">
            <form onSubmit={handleSendMessage} className="input-form">
              <div className="input-wrapper">
                <button type="button" className="attach-btn">📎</button>
                {inputMessage.trim() === '' && (
                  <button 
                    type="button" 
                    className="prompt-select-btn" 
                    onClick={() => setShowPromptSelector(true)}
                    disabled={isStreaming}
                  >
                    프롬프트 선택하기
                  </button>
                )}
                <textarea
                  ref={textareaRef}
                  value={inputMessage}
                  onChange={(e) => setInputMessage(e.target.value)}
                  onKeyPress={handleKeyPress}
                  placeholder={isStreaming ? "AI가 응답 중입니다..." : "무엇이든 물어보세요"}
                  className="message-input"
                  rows="1"
                  disabled={isStreaming}
                />
                <button 
                  type="submit" 
                  className={`send-btn ${inputMessage.trim() && !isStreaming ? 'active' : ''}`}
                  disabled={!inputMessage.trim() || isStreaming}
                >
                  {isStreaming ? '⏸️' : '↑'}
                </button>
              </div>
            </form>
          </div>
          <p className="disclaimer">
            ChatGPT가 실수를 할 수 있습니다. 중요한 정보를 확인하세요.
          </p>
        </div>
      </div>
    </div>
  );
};

export default Chat; 