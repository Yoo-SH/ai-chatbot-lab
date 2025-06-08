# 🤖 ChatGPT 클론 서비스

React와 Spring Boot로 구현한 완전한 ChatGPT 클론 애플리케이션입니다. OpenAI GPT-4 API와 실시간 스트리밍을 통해 원본과 동일한 사용자 경험을 제공합니다.

## 🌟 주요 특징

- ✨ **완벽한 ChatGPT UI 클론**: 원본과 동일한 다크 테마와 말풍선 디자인
- 🔄 **실시간 스트리밍**: Server-Sent Events를 통한 타이핑 애니메이션
- 📚 **프롬프트 관리**: 사용자 정의 프롬프트 생성, 편집, 삭제
- 🗂️ **스마트 채팅 관리**: 대화 기록 저장, 빈 채팅 자동 삭제, 개별 삭제
- 🔐 **JWT 인증**: 안전한 사용자 인증 및 개인별 데이터 관리
- 📱 **완전 반응형**: 모든 기기에서 최적화된 사용자 경험

## 🏗️ 시스템 아키텍처

```
Frontend (React) ↔ Backend (Spring Boot) ↔ OpenAI GPT-4 API
                           ↓
                 PostgreSQL + Redis
```

## 🛠️ 기술 스택

### Frontend
- **React 18** (Hooks, Functional Components)
- **React Router DOM** (SPA 라우팅)
- **Modern CSS3** (Flexbox, Grid, Animations)
- **ES6+ JavaScript** (Async/Await, Modules)
- **Fetch API** (HTTP 통신)

### Backend  
- **Spring Boot 3.5** (REST API)
- **Spring Security** (JWT 인증)
- **Spring Data JPA** (ORM)
- **PostgreSQL** (메인 데이터베이스)
- **Redis** (캐싱 & 세션 관리)
- **Maven** (의존성 관리)

### AI & External APIs
- **OpenAI GPT-4 API** (AI 대화)
- **Server-Sent Events** (실시간 스트리밍)
- **RESTful API** 설계
- **JWT** (JSON Web Token)

## RDB 구조

![Image](https://github.com/user-attachments/assets/52dfae7b-a67d-4c80-9751-7aad38a4aa8e)


## 🚀 빠른 시작

### 1. 필수 요구사항

- **Java 17+**
- **Node.js 18+**
- **PostgreSQL 13+**
- **Redis 6+**
- **OpenAI API Key**

### 2. 환경 설정

#### OpenAI API Key 설정
```bash
# 백엔드 application.yml에 추가
openai:
  api-key: "your-openai-api-key-here"
```

#### 데이터베이스 설정
```bash
# PostgreSQL 데이터베이스 생성
createdb chatgpt_clone

# Redis 서버 시작
redis-server
```

### 3. 서비스 실행

#### Step 1: 백엔드 서비스 시작
```bash
cd back/gpt
./gradlew bootRun
```
- 실행 후: http://localhost:8080

#### Step 2: 프론트엔드 서비스 시작
```bash
cd front
npm install
npm start
```
- 실행 후: http://localhost:3000

## 📋 주요 기능

### 🔐 사용자 인증
- JWT 기반 회원가입/로그인
- 개인별 채팅 기록 관리
- 안전한 세션 관리

### 💬 AI 채팅
- OpenAI GPT-4와 실시간 대화
- 타이핑 애니메이션과 스트리밍 응답
- 메시지 기록 자동 저장

### 📚 프롬프트 관리
- 기본 프롬프트 3개 제공
- 사용자 정의 프롬프트 생성/편집/삭제
- 프롬프트별 색상 구분 (기본: 녹색, 사용자: 보라색)

### 🗂️ 채팅 관리
- 사이드바에서 채팅 목록 확인
- 개별 채팅 삭제 기능 (🗑️ 버튼)
- 빈 채팅 자동 삭제 시스템
- 시간별 채팅 그룹화

### 🎨 UI/UX
- ChatGPT와 동일한 다크 테마
- 말풍선 스타일 메시지 디자인
- 호버 애니메이션 및 트랜지션
- 완전 반응형 디자인

## 🔄 API 엔드포인트

### 인증 API
```
POST /api/auth/register    # 회원가입
POST /api/auth/login       # 로그인  
POST /api/auth/logout      # 로그아웃
```

### 채팅 API
```
GET  /api/conversations           # 채팅 목록 조회
POST /api/conversations           # 새 채팅 생성
DELETE /api/conversations/{id}    # 채팅 삭제

POST /api/messages                # 메시지 전송
GET  /api/messages/stream/{id}    # 실시간 스트리밍 (SSE)
```

### 프롬프트 API
```
GET    /api/prompts         # 프롬프트 목록 조회
POST   /api/prompts         # 프롬프트 생성
PUT    /api/prompts/{id}    # 프롬프트 수정
DELETE /api/prompts/{id}    # 프롬프트 삭제
```

## 📁 프로젝트 구조

```
📁 chat-gpt-clone-coding/
├── 📁 back/                    # Spring Boot 백엔드
│   └── 📁 gpt/
│       ├── 📁 src/main/java/com/example/gpt/
│       │   ├── 📁 config/      # 설정 클래스
│       │   ├── 📁 controller/  # REST 컨트롤러
│       │   ├── 📁 entity/      # JPA 엔티티
│       │   ├── 📁 repository/  # 데이터 레포지토리
│       │   ├── 📁 service/     # 비즈니스 로직
│       │   └── 📁 dto/         # 데이터 전송 객체
│       └── 📄 application.yml  # 애플리케이션 설정
├── 📁 front/                   # React 프론트엔드
│   ├── 📁 public/
│   └── 📁 src/
│       ├── 📁 components/      # React 컴포넌트
│       ├── 📁 pages/          # 페이지 컴포넌트
│       ├── 📁 services/       # API 서비스
│       └── 📁 utils/          # 유틸리티 함수
└── 📄 README.md
```

## ⚙️ 설정 가이드

### application.yml 설정 예시
```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/chatgpt_clone
    username: your_username
    password: your_password
  
  redis:
    host: localhost
    port: 6379
  
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

openai:
  api-key: ${OPENAI_API_KEY}
  model: gpt-4

jwt:
  secret: your-jwt-secret-key
  expiration: 86400000
```

## 🚨 문제 해결

### 일반적인 문제들

#### 1. OpenAI API 오류
```bash
# API 키 확인
echo $OPENAI_API_KEY

# 사용량 한도 확인
# https://platform.openai.com/usage
```

#### 2. 데이터베이스 연결 실패
```bash
# PostgreSQL 상태 확인
pg_isready

# 데이터베이스 존재 확인
psql -l | grep chatgpt_clone
```

#### 3. Redis 연결 실패
```bash
# Redis 상태 확인
redis-cli ping

# Redis 서버 시작
redis-server
```

### 개발 환경 초기화
```bash
# 전체 재설치
cd front && npm install
cd back/gpt && ./gradlew clean build

# 캐시 정리
npm cache clean --force
./gradlew clean
```

## 🎯 주요 개발 성과

- ✅ **React 18** 최신 Hook 시스템 활용
- ✅ **Spring Boot 3.5** RESTful API 설계
- ✅ **JWT** 기반 보안 인증 구현
- ✅ **Server-Sent Events** 실시간 스트리밍 구현
- ✅ **PostgreSQL + Redis** 데이터베이스 설계
- ✅ **픽셀 퍼펙트** ChatGPT UI 클론
- ✅ **완전 반응형** 웹 디자인

## 📊 프로젝트 통계

| 항목 | 내용 |
|------|------|
| **UI 재현도** | 100% ChatGPT 동일 |
| **핵심 기능** | 15+ 완전 구현 |
| **기술 스택** | 10+ 최신 기술 |
| **반응형 지원** | 모든 기기 대응 |

## 🤝 기여하기

1. Repository를 Fork
2. Feature 브랜치 생성 (`git checkout -b feature/amazing-feature`)
3. 변경사항 커밋 (`git commit -m 'Add amazing feature'`)
4. 브랜치에 Push (`git push origin feature/amazing-feature`)
5. Pull Request 생성

## 📄 라이센스

이 프로젝트는 MIT 라이센스를 따릅니다.

## 🔗 관련 링크

- **GitHub Repository**: [https://github.com/Yoo-SH](https://github.com/Yoo-SH)
- **OpenAI API 문서**: [https://platform.openai.com/docs](https://platform.openai.com/docs)
- **Spring Boot 문서**: [https://spring.io/projects/spring-boot](https://spring.io/projects/spring-boot)
- **React 문서**: [https://react.dev](https://react.dev)

---

**Made with ❤️ by ChatGPT Clone Team**
