import React, { useState, useEffect } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import './Navigation.css';

const Navigation = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [user, setUser] = useState(null);

  // 로그인 상태 확인
  useEffect(() => {
    const token = localStorage.getItem('accessToken');
    const userData = localStorage.getItem('user');
    
    if (token) {
      setIsLoggedIn(true);
      if (userData) {
        setUser(JSON.parse(userData));
      }
    } else {
      setIsLoggedIn(false);
      setUser(null);
    }
  }, [location]);

  // 로그아웃 처리
  const handleLogout = async () => {
    try {
      const token = localStorage.getItem('accessToken');
      
      if (token) {
        // 백엔드에 로그아웃 요청
        await fetch('http://localhost:8080/api/auth/logout', {
          method: 'POST',
          headers: {
            'Authorization': `Bearer ${token}`,
          },
        });
      }
    } catch (error) {
      console.error('로그아웃 요청 실패:', error);
    } finally {
      // 로컬 스토리지 정리
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('user');
      
      setIsLoggedIn(false);
      setUser(null);
      
      // 홈페이지로 리다이렉트
      navigate('/');
    }
  };

  return (
    <nav className="navigation">
      <div className="nav-container">
        <div className="nav-logo">
          <Link to="/">
            🤖 ChatGPT Clone
          </Link>
        </div>
        
        <ul className="nav-menu">
          <li className="nav-item">
            <Link 
              to="/" 
              className={`nav-link ${location.pathname === '/' ? 'active' : ''}`}
            >
              🏠 홈
            </Link>
          </li>
          <li className="nav-item">
            <Link 
              to="/about" 
              className={`nav-link ${location.pathname === '/about' ? 'active' : ''}`}
            >
              ℹ️ 소개
            </Link>
          </li>
          {isLoggedIn && (
            <li className="nav-item">
              <Link 
                to="/chat" 
                className={`nav-link ${location.pathname === '/chat' ? 'active' : ''}`}
              >
                💬 채팅
              </Link>
            </li>
          )}
        </ul>

        <div className="nav-auth">
          {isLoggedIn ? (
            <div className="user-menu">
              <span className="user-info">
                👋 안녕하세요, {user?.name || user?.email || '사용자'}님!
              </span>
              <button 
                onClick={handleLogout}
                className="logout-button"
              >
                🚪 로그아웃
              </button>
            </div>
          ) : (
            <div className="auth-buttons">
              <Link 
                to="/login" 
                className="login-button"
              >
                🔑 로그인
              </Link>
              <Link 
                to="/register" 
                className="register-button"
              >
                📝 회원가입
              </Link>
            </div>
          )}
        </div>
      </div>
    </nav>
  );
};

export default Navigation; 