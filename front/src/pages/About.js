import React from 'react';
import './About.css';

const About = () => {
  return (
    <div className="about-container">
      <div className="about-content">
        <h1>ChatGPT 클론 프로젝트</h1>
        
        <section className="about-section">
          <h2>🚀 프로젝트 소개</h2>
          <p>
            이 프로젝트는 OpenAI의 ChatGPT와 동일한 사용자 경험을 제공하는 완전한 풀스택 AI 채팅 애플리케이션입니다. 
            React 18과 Spring Boot를 기반으로 현대적이고 확장 가능한 아키텍처를 구현했으며, 
            실제 ChatGPT와 동일한 UI/UX, 실시간 스트리밍, 그리고 완전한 채팅 관리 시스템을 제공합니다.
          </p>
        </section>

        <section className="about-section">
          <h2>🛠️ 기술 스택</h2>
          <div className="tech-stack">
            <div className="tech-item">
              <h3>Frontend</h3>
              <ul>
                <li>React 18 (Hooks, Functional Components)</li>
                <li>React Router DOM (SPA 라우팅)</li>
                <li>Modern CSS3 (Flexbox, Grid, Animations)</li>
                <li>ES6+ JavaScript (Async/Await, Modules)</li>
                <li>반응형 웹 디자인 (Mobile-First)</li>
                <li>Fetch API (HTTP 통신)</li>
              </ul>
            </div>
            <div className="tech-item">
              <h3>Backend</h3>
              <ul>
                <li>Spring Boot 3.5 (REST API)</li>
                <li>Spring Security (JWT 인증)</li>
                <li>Redis Database (Production)</li>
                <li>PostgreSQL Database (Production)</li>
                <li>Spring Data JPA (ORM)</li>
                <li>Maven (의존성 관리)</li>
              </ul>
            </div>
            <div className="tech-item">
              <h3>Open AI</h3>
              <ul>
                <li>OpenAI GPT-3 API</li>
                <li>text streaming 처리</li>
              </ul>
            </div>
            <div className="tech-item">
              <h3> FastAPI</h3>
              <ul>
                <li>FastAPI</li>
                <li>Server-Sent Events (실시간 스트리밍)</li>
                <li>RESTful API 설계</li>
                <li>JSON Web Token (JWT)</li>
              </ul>
            </div>
          </div> 
        </section>

        <section className="about-section">
          <h2>✨ 주요 기능</h2>
          <div className="features-grid">
            <div className="feature-item">
              <h3 data-icon="💬">실시간 AI 채팅</h3>
              <p>OpenAI GPT-4와 실시간 스트리밍 대화, 타이핑 애니메이션과 즉시 응답을 제공합니다</p>
            </div>
            <div className="feature-item">
              <h3 data-icon="📚">프롬프트 관리</h3>
              <p>사용자 정의 프롬프트 생성, 편집, 삭제 기능으로 개인화된 AI 상호작용이 가능합니다</p>
            </div>
            <div className="feature-item">
              <h3 data-icon="🗂️">스마트 채팅 관리</h3>
              <p>대화 기록 저장, 시간별 그룹화, 빈 채팅 자동 삭제, 개별 채팅 삭제 기능</p>
            </div>
            <div className="feature-item">
              <h3 data-icon="🔐">사용자 인증</h3>
              <p>JWT 기반 보안 로그인, 개인별 채팅 기록 관리, 안전한 API 통신</p>
            </div>
            <div className="feature-item">
              <h3 data-icon="🎨">ChatGPT 스타일 UI</h3>
              <p>원본과 동일한 다크 테마, 말풍선 디자인, 직관적인 사이드바 네비게이션</p>
            </div>
            <div className="feature-item">
              <h3 data-icon="📱">완전 반응형</h3>
              <p>데스크톱, 태블릿, 모바일 모든 기기에서 최적화된 사용자 경험</p>
            </div>
          </div>
        </section>

        <section className="about-section">
          <h2>⚡ 핵심 기술 구현</h2>
          <div className="tech-details">
            <div className="tech-detail-item">
              <h4>🔄 실시간 스트리밍</h4>
              <p>Server-Sent Events를 활용한 실시간 AI 응답 스트리밍으로 ChatGPT와 동일한 사용자 경험 구현</p>
            </div>
            <div className="tech-detail-item">
              <h4>🏗️ 컴포넌트 기반 아키텍처</h4>
              <p>재사용 가능한 React 컴포넌트와 Spring Boot의 계층형 아키텍처로 확장성과 유지보수성 확보</p>
            </div>
            <div className="tech-detail-item">
              <h4>🎯 상태 관리</h4>
              <p>React Hooks를 활용한 효율적인 상태 관리와 사이드 이펙트 처리</p>
            </div>
            <div className="tech-detail-item">
              <h4>🛡️ 보안</h4>
              <p>Spring Security와 JWT를 통한 안전한 인증 시스템과 API 보호</p>
            </div>
          </div>
        </section>

        <section className="about-section">
          <h2>👨‍💻 개발 하이라이트</h2>
          <p>
            이 프로젝트는 현대적인 풀스택 웹 개발의 모든 측면을 포괄하는 종합적인 학습 프로젝트입니다. 
            프론트엔드에서는 React 18의 최신 기능과 현대적인 CSS 기법을 활용했으며, 
            백엔드에서는 Spring Boot의 강력한 기능들을 통해 확장 가능한 REST API를 구축했습니다.
          </p>
          <p>
            특히 실제 ChatGPT의 사용자 경험을 완벽하게 재현하는데 중점을 두었으며, 
            실시간 스트리밍, 말풍선 UI, 프롬프트 관리 등 실무에서 요구되는 
            고급 기능들을 모두 구현하여 포트폴리오 수준의 완성도를 달성했습니다.
          </p>
        </section>

        <section className="about-section">
          <h2>🎯 학습 성과</h2>
          <ul className="learning-outcomes">
            <li>✅ React 18의 최신 기능과 훅 시스템 마스터</li>
            <li>✅ Spring Boot를 활용한 RESTful API 설계 및 구현</li>
            <li>✅ JWT 기반 인증 시스템 구축</li>
            <li>✅ 실시간 데이터 스트리밍 기술 적용</li>
            <li>✅ 데이터베이스 설계 및 JPA 활용</li>
            <li>✅ 현대적인 UI/UX 디자인 구현</li>
            <li>✅ 풀스택 개발 프로세스 경험</li>
          </ul>
        </section>

        <div className="project-highlight">
          <h3>🌟 프로젝트 성과</h3>
          <p>
            OpenAI ChatGPT를 완벽하게 클론한 풀스택 웹 애플리케이션으로, 
            현대적인 React와 Spring Boot 기술을 활용하여 실무 수준의 완성도를 달성했습니다.
          </p>
          <div className="stats">
            <div className="stat-item">
              <span className="stat-number">Ui</span>
              <span className="stat-label">ChatGPT 디자인 클론</span>
            </div>
            <div className="stat-item">
              <span className="stat-number">15+</span>
              <span className="stat-label">핵심 기능 구현</span>
            </div>
            <div className="stat-item">
              <span className="stat-number">반응형</span>
              <span className="stat-label">모든 기기 지원</span>
            </div>
          </div>
          <a href="https://github.com/Yoo-SH" className="github-link" target="_blank" rel="noopener noreferrer">GitHub 저장소 바로가기</a>
        </div>
      </div>
    </div>
  );
};

export default About; 