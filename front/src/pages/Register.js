import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import './Register.css';

const Register = () => {
  const [formData, setFormData] = useState({
    email: '',
    password: '',
    confirmPassword: ''
  });
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const navigate = useNavigate();

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
    // 입력할 때 에러 메시지 초기화
    if (error) setError('');
    if (success) setSuccess('');
  };

  const validateForm = () => {
    if (!formData.email || !formData.password || !formData.confirmPassword) {
      setError('모든 필드를 입력해주세요.');
      return false;
    }

    if (formData.password.length < 6) {
      setError('비밀번호는 6자리 이상이어야 합니다.');
      return false;
    }

    if (formData.password !== formData.confirmPassword) {
      setError('비밀번호가 일치하지 않습니다.');
      return false;
    }

    return true;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!validateForm()) return;

    setIsLoading(true);
    setError('');
    setSuccess('');

    try {
      const response = await fetch('http://localhost:8080/api/auth/register', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          email: formData.email,
          password: formData.password
        })
      });

      const data = await response.json();

      if (response.ok) {
        setSuccess('회원가입이 완료되었습니다! 로그인 페이지로 이동합니다.');
        
        // 2초 후 로그인 페이지로 이동
        setTimeout(() => {
          navigate('/login');
        }, 2000);
      } else {
        setError(data.message || '회원가입에 실패했습니다.');
      }
    } catch (error) {
      console.error('회원가입 에러:', error);
      setError('서버 연결에 실패했습니다. 잠시 후 다시 시도해주세요.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="register-container">
      <div className="register-wrapper">
        <div className="register-card">
          <div className="register-header">
            <h1 className="register-title">회원가입</h1>
            <p className="register-subtitle">새로운 계정을 만들어 ChatGPT 클론을 시작해보세요!</p>
          </div>

          <form onSubmit={handleSubmit} className="register-form">
            <div className="form-group">
              <label htmlFor="email" className="form-label">이메일</label>
              <input
                type="email"
                id="email"
                name="email"
                value={formData.email}
                onChange={handleChange}
                className="form-input"
                placeholder="이메일을 입력하세요"
                required
                disabled={isLoading}
              />
            </div>

            <div className="form-group">
              <label htmlFor="password" className="form-label">비밀번호</label>
              <input
                type="password"
                id="password"
                name="password"
                value={formData.password}
                onChange={handleChange}
                className="form-input"
                placeholder="6자리 이상의 비밀번호를 입력하세요"
                required
                disabled={isLoading}
                minLength={6}
              />
              <div className="password-strength">
                {formData.password && (
                  <div className="strength-indicators">
                    <div className={`strength-indicator ${formData.password.length >= 6 ? 'valid' : 'invalid'}`}>
                      {formData.password.length >= 6 ? '✓' : '✗'} 6자리 이상
                    </div>
                  </div>
                )}
              </div>
            </div>

            <div className="form-group">
              <label htmlFor="confirmPassword" className="form-label">비밀번호 확인</label>
              <input
                type="password"
                id="confirmPassword"
                name="confirmPassword"
                value={formData.confirmPassword}
                onChange={handleChange}
                className="form-input"
                placeholder="비밀번호를 다시 입력하세요"
                required
                disabled={isLoading}
              />
              {formData.confirmPassword && (
                <div className="password-match">
                  <span className={`match-indicator ${formData.password === formData.confirmPassword ? 'match' : 'no-match'}`}>
                    {formData.password === formData.confirmPassword ? '✓ 비밀번호가 일치합니다' : '✗ 비밀번호가 일치하지 않습니다'}
                  </span>
                </div>
              )}
            </div>

            {error && (
              <div className="error-message">
                <span className="error-icon">⚠️</span>
                {error}
              </div>
            )}

            {success && (
              <div className="success-message">
                <span className="success-icon">✅</span>
                {success}
              </div>
            )}

            <button
              type="submit"
              className={`register-button ${isLoading ? 'loading' : ''}`}
              disabled={isLoading}
            >
              {isLoading ? (
                <>
                  <span className="loading-spinner"></span>
                  회원가입 중...
                </>
              ) : (
                '회원가입'
              )}
            </button>
          </form>

          <div className="register-footer">
            <p className="footer-text">
              이미 계정이 있으신가요?{' '}
              <Link to="/login" className="footer-link">
                로그인
              </Link>
            </p>
          </div>
        </div>

        <div className="register-background">
          <div className="background-pattern"></div>
        </div>
      </div>
    </div>
  );
};

export default Register; 