# 🤖 ChatGPT 클론 서비스

React, Spring Boot, FastAPI로 구현한 ChatGPT 클론 애플리케이션입니다. OpenAI GPT-4 API와 실시간 스트리밍을 통해 원본과 동일한 사용자 경험을 제공합니다.

## 🌟 주요 특징

- ✨ **완벽한 ChatGPT UI 클론**: 원본과 동일한 다크 테마와 말풍선 디자인
- 🔄 **실시간 스트리밍**: Server-Sent Events를 통한 타이핑 애니메이션
- 📚 **지능형 프롬프트 관리**: 중앙집중식 프롬프트 설정, 최적화된 AI 매개변수 관리
- 🎯 **프롬프트별 최적화**: 용도별 temperature, max_tokens 자동 조정
- 🗂️ **스마트 채팅 관리**: 대화 기록 저장, 빈 채팅 자동 삭제, 개별 삭제
- 🔐 **JWT 인증**: 안전한 사용자 인증 및 개인별 데이터 관리
- 📱 **완전 반응형**: 모든 기기에서 최적화된 사용자 경험
- 🐳 **Docker 컨테이너화**: 간편한 배포 및 환경 관리

## 🏗️ 시스템 아키텍처

```
Frontend (React) ↔ Backend (Spring Boot) ↔ AI Service (FastAPI) ↔ OpenAI GPT-4 API
                           ↓
                 PostgreSQL + Redis
```

## 🛠️ 기술 스택

### Frontend (React)
- **React 18** (Hooks, Functional Components)
- **React Router DOM** (SPA 라우팅)
- **Modern CSS3** (Flexbox, Grid, Animations)
- **ES6+ JavaScript** (Async/Await, Modules)
- **Fetch API** (HTTP 통신)

### Backend (Spring Boot)
- **Spring Boot 3.5** (REST API)
- **Spring Security** (JWT 인증)
- **Spring Data JPA** (ORM)
- **PostgreSQL** (메인 데이터베이스)
- **Redis** (캐싱 & 세션 관리)
- **Gradle** (의존성 관리)

### AI Service (FastAPI)
- **FastAPI** (Python 웹 프레임워크)
- **OpenAI GPT-4 API** (AI 대화)
- **Server-Sent Events** (실시간 스트리밍)
- **지능형 프롬프트 시스템** (CONFIG 기반 중앙 관리)
- **최적화된 AI 매개변수** (용도별 temperature, max_tokens 조정)
- **Pydantic** (데이터 검증)
- **Uvicorn** (ASGI 서버)

### Infrastructure
- **Docker & Docker Compose** (컨테이너화)
- **Nginx** (리버스 프록시)
- **RESTful API** 설계
- **JWT** (JSON Web Token)

## 🚀 빠른 시작

### 1. 필수 요구사항

- **Docker & Docker Compose**
- **OpenAI API Key**
- **Git**

### 2. 프로젝트 설정

```bash
# 프로젝트 클론
git clone <repository-url>
cd chat-gpt-clone-coding

# 환경 변수 설정
cp env.example .env
# .env 파일에서 OPENAI_API_KEY 설정
```

### 3. Docker로 전체 서비스 실행

```bash
# 자동 빌드 및 실행 스크립트 사용
./starts.sh

# 또는 수동으로 실행
docker-compose up -d --build
```

### 4. 개별 서비스 실행 (개발 모드)

#### AI 서비스 (FastAPI)
```bash
cd ai
python setup_venv.py  # 가상환경 설정
source venv/bin/activate  # Windows: venv\Scripts\activate
uvicorn ai_api.api_server:app --reload --port 8000
```

#### 백엔드 (Spring Boot)
```bash
cd back/gpt
./gradlew bootRun
```

#### 프론트엔드 (React)
```bash
cd front
npm install
npm start
```

## 📋 서비스 접속 정보

- **프론트엔드**: http://localhost:3000
- **백엔드 API**: http://localhost:8080
- **AI 서비스**: http://localhost:8000
- **AI API 문서**: http://localhost:8000/docs
- **PostgreSQL**: localhost:5432
- **Redis**: localhost:6379
- **Redis Commander**: http://localhost:8081

## 📁 프로젝트 구조

```
📁 chat-gpt-clone-coding/
├── 📁 ai/                     # FastAPI AI 서비스
│   ├── 📁 ai_api/             # API 서버 모듈
│   │   ├── api_server.py      # FastAPI 서버
│   │   └── openai_service.py  # OpenAI API 서비스
│   ├── 📁 ai_cli/             # CLI 도구 모듈
│   │   ├── chatbot.py         # 대화형 챗봇
│   │   └── examples.py        # 사용 예제
│   ├── prompt.py              # 지능형 프롬프트 관리 시스템
│   ├── main.py                # CLI 통합 메뉴
│   ├── Dockerfile             # AI 서비스 컨테이너 설정
│   └── requirements.txt       # Python 의존성
├── 📁 back/                   # Spring Boot 백엔드
│   └── 📁 gpt/
│       ├── 📁 src/main/java/com/clone/gpt/
│       │   ├── 📁 config/     # 설정 클래스
│       │   ├── 📁 controller/ # REST 컨트롤러
│       │   ├── 📁 entity/     # JPA 엔티티
│       │   ├── 📁 repository/ # 데이터 레포지토리
│       │   ├── 📁 service/    # 비즈니스 로직
│       │   └── 📁 model/dto/  # 데이터 전송 객체
│       ├── build.gradle       # Gradle 빌드 설정
│       └── Dockerfile         # 백엔드 컨테이너 설정
├── 📁 front/                  # React 프론트엔드
│   ├── 📁 src/
│   │   ├── 📁 components/     # React 컴포넌트
│   │   ├── 📁 pages/         # 페이지 컴포넌트
│   │   └── 📁 services/      # API 서비스
│   ├── package.json          # Node.js 의존성
│   └── Dockerfile            # 프론트엔드 컨테이너 설정
├── 📁 db-init/               # 데이터베이스 초기화
├── docker-compose.yml        # Docker Compose 설정
├── starts.sh                 # 자동 실행 스크립트
└── README.md
```

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

### AI 서비스 API
```
POST /api/chat              # AI 채팅 요청
GET  /api/chat/stream       # 스트리밍 채팅 (SSE)
GET  /health               # 헬스 체크
```

## ⚙️ 환경 설정

### .env 파일 설정
```env
# OpenAI API 설정
OPENAI_API_KEY=your-openai-api-key-here

# 데이터베이스 설정
POSTGRES_DB=gpt_clone_dev
POSTGRES_USER=postgres
POSTGRES_PASSWORD=3482

# JWT 설정
JWT_SECRET=mySecretKey123456789012345678901234567890

# AI 서비스 설정
AI_SERVICE_BASE_URL=http://ai-service:8000
AI_SERVICE_TIMEOUT=30

# 기본 AI 모델 설정 (프롬프트별 최적화 설정은 prompt.py에서 관리)
DEFAULT_MODEL=gpt-3.5-turbo
# 참고: temperature, max_tokens는 이제 프롬프트별로 자동 최적화됩니다
```

### Spring Boot 설정 (application.yml)
```yaml
server:
  port: 8080

spring:
  profiles:
    active: docker
  datasource:
    url: jdbc:postgresql://postgres:5432/gpt_clone_dev
    username: postgres
    password: 3482
  data:
    redis:
      host: redis
      port: 6379
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

jwt:
  secret: ${JWT_SECRET}
  expiration: 86400000
```

## 🐳 Docker 사용법

### starts.sh 스크립트 옵션
```bash
./starts.sh -h              # 도움말
./starts.sh -d              # 개발 모드 실행
./starts.sh -p              # 프로덕션 모드 실행
./starts.sh -b              # 강제 재빌드
./starts.sh -c              # 컨테이너 정리
./starts.sh --fix-ports     # 포트 충돌 해결
./starts.sh --status        # 서비스 상태 확인
```

### 수동 Docker 명령어
```bash
# 전체 서비스 실행
docker-compose up -d --build

# 로그 확인
docker-compose logs -f

# 특정 서비스만 실행
docker-compose up -d postgres redis ai-service

# 서비스 중지
docker-compose down

# 볼륨까지 삭제 (데이터 초기화)
docker-compose down -v
```

## 📋 주요 기능

### 🔐 사용자 인증
- JWT 기반 회원가입/로그인
- 개인별 채팅 기록 관리
- 안전한 세션 관리

### 💬 AI 채팅
- OpenAI GPT-4와 실시간 대화
- 타이핑 애니메이션과 스트리밍 응답
- 메시지 기록 자동 저장
- 프롬프트별 자동 최적화된 AI 응답

### 📚 지능형 프롬프트 관리
- **중앙집중식 관리**: `prompt.py`에서 모든 프롬프트와 설정 통합 관리
- **프롬프트별 최적화**: 용도에 맞는 temperature, max_tokens 자동 적용
- **CONFIG 기반 구조**: 체계적인 프롬프트-설정 묶음 관리
- **헬퍼 함수 제공**: `get_config()`, `get_prompt()`, `get_temperature()`, `get_max_tokens()`
- **하위 호환성**: 기존 코드 수정 없이 새 기능 활용 가능
- **확장성**: 새로운 프롬프트 타입 쉽게 추가 가능

#### 지원하는 프롬프트 타입
| 타입 | Temperature | Max Tokens | 용도 |
|------|-------------|------------|------|
| **일반 채팅** | 0.7 | 2000 | 균형잡힌 일반 대화 |
| **코드 어시스턴트** | 0.2 | 3000 | 정확한 프로그래밍 지원 |
| **대화 제목 생성** | 0.3 | 50 | 일관된 제목 생성 |
| **감정 분석** | 0.4 | 1500 | 감정 분석 및 코칭 |

### 🗂️ 채팅 관리
- 사이드바에서 채팅 목록 확인
- 개별 채팅 삭제 기능
- 빈 채팅 자동 삭제 시스템
- 시간별 채팅 그룹화

### 🎨 UI/UX
- ChatGPT와 동일한 다크 테마
- 말풍선 스타일 메시지 디자인
- 호버 애니메이션 및 트랜지션
- 완전 반응형 디자인

## 🚨 문제 해결

### 일반적인 문제들

#### 1. OpenAI API 오류
```bash
# API 키 확인
echo $OPENAI_API_KEY

# .env 파일 확인
cat .env
```

#### 2. 포트 충돌
```bash
# 포트 충돌 자동 해결
./starts.sh --fix-ports

# 수동으로 포트 확인
netstat -an | grep :8080
```

#### 3. Docker 컨테이너 문제
```bash
# 컨테이너 상태 확인
docker-compose ps

# 로그 확인
docker-compose logs [service-name]

# 컨테이너 재시작
docker-compose restart [service-name]
```

#### 4. 데이터베이스 연결 실패
```bash
# PostgreSQL 상태 확인
docker-compose exec postgres pg_isready

# 데이터베이스 접속 테스트
docker-compose exec postgres psql -U postgres -d gpt_clone_dev
```

### 개발 환경 초기화
```bash
# 전체 정리 및 재시작
./starts.sh -c && ./starts.sh -b

# 개별 서비스 재설치
cd front && npm install
cd back/gpt && ./gradlew clean build
cd ai && python setup_venv.py
```

### CLI 챗봇에서 프롬프트 변경
```bash
# 고급 챗봇 실행
cd ai
python main.py

# 챗봇에서 명령어 사용
set-prompt 당신은 친근한 한국어 튜터입니다.
set-temp 0.5
set-tokens 1500
show-settings  # 현재 설정 확인
```

## 🎯 주요 개발 성과

- ✅ **마이크로서비스 아키텍처**: AI 서비스 분리로 확장성 확보
- ✅ **지능형 프롬프트 시스템**: CONFIG 기반 중앙집중식 관리
- ✅ **프롬프트별 AI 최적화**: 용도별 매개변수 자동 조정
- ✅ **React 18** 최신 Hook 시스템 활용
- ✅ **Spring Boot 3.5** RESTful API 설계
- ✅ **FastAPI** 고성능 AI 서비스 구현
- ✅ **JWT** 기반 보안 인증 구현
- ✅ **Server-Sent Events** 실시간 스트리밍 구현
- ✅ **PostgreSQL + Redis** 데이터베이스 설계
- ✅ **Docker 컨테이너화** 완전 자동화
- ✅ **픽셀 퍼펙트** ChatGPT UI 클론

## 📊 프로젝트 통계

| 항목 | 내용 |
|------|------|
| **UI 재현도** | 100% ChatGPT 동일 |
| **핵심 기능** | 15+ 완전 구현 |
| **프롬프트 관리** | 지능형 CONFIG 시스템 |
| **AI 최적화** | 4가지 타입별 자동 조정 |
| **기술 스택** | 12+ 최신 기술 |
| **마이크로서비스** | 3계층 아키텍처 |
| **컨테이너화** | 100% Docker 지원 |
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

- **OpenAI API 문서**: [https://platform.openai.com/docs](https://platform.openai.com/docs)
- **Spring Boot 문서**: [https://spring.io/projects/spring-boot](https://spring.io/projects/spring-boot)
- **React 문서**: [https://react.dev](https://react.dev)
- **FastAPI 문서**: [https://fastapi.tiangolo.com](https://fastapi.tiangolo.com)
- **Docker 문서**: [https://docs.docker.com](https://docs.docker.com)

---

**Made with ❤️ by ChatGPT Clone Team**
