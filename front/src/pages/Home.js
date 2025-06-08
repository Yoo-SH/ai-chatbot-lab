import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import './Home.css';

const Home = () => {
  const [isLoggedIn, setIsLoggedIn] = useState(false);

  // 로그인 상태 확인
  useEffect(() => {
    const token = localStorage.getItem('accessToken');
    setIsLoggedIn(!!token);
  }, []);

  return (
    <div className="home-container">
      <div className="hero-section">
        <h1>ChatGPT Clone에 오신 것을 환영합니다</h1>
        <p>AI와 함께 대화하며 새로운 가능성을 경험해보세요</p>
        
        <div className="features">
          <div className="feature-card">
            <h3 data-icon="🤖">AI 대화</h3>
            <p>자연스러운 대화를 통해 궁금한 것을 물어보세요. 복잡한 질문부터 간단한 도움까지 모든 것을 지원합니다.</p>
          </div>
          <div className="feature-card">
            <h3 data-icon="💡">스마트 도움말</h3>
            <p>학습, 업무, 창작 활동 등 다양한 분야에서 개인화된 지원을 받을 수 있습니다.</p>
          </div>
          <div className="feature-card">
            <h3 data-icon="📝">텍스트 생성</h3>
            <p>이메일 작성부터 창의적 글쓰기까지, AI가 도와드리는 텍스트 생성 기능을 활용해보세요.</p>
          </div>
        </div>
      </div>

      <div className="cta-section">
        <h2>지금 바로 시작해보세요</h2>
        <p>
          최신 AI 기술을 기반으로 한 대화형 인터페이스를 경험해보세요. 
          복잡한 설정 없이 바로 사용할 수 있습니다.
        </p>
        
        {isLoggedIn ? (
          <Link to="/chat" className="cta-button">
            채팅 시작하기
          </Link>
        ) : (
          <div className="auth-buttons-home">
            <Link to="/login" className="cta-button primary">
              로그인하고 시작하기
            </Link>
            <Link to="/register" className="cta-button secondary">
              회원가입
            </Link>
          </div>
        )}
      </div>
    </div>
  );
};

export default Home; 