# 🐳 Docker 배포 가이드

ChatGPT 클론 프로젝트를 Docker Compose로 간편하게 실행하는 방법을 설명합니다.

## 📋 필수 요구사항

- **Docker** 20.10+
- **Docker Compose** 2.0+
- **OpenAI API Key**
- 최소 **4GB RAM**, **2GB** 여유 디스크 공간

## 🚀 빠른 시작

### 1. 환경 변수 설정

```bash
# env.example을 복사하여 .env 파일 생성
cp env.example .env

# .env 파일에서 OpenAI API 키 설정
# OPENAI_API_KEY=your-actual-api-key-here
```

### 2. 전체 서비스 실행

```bash
# 전체 스택 실행 (백그라운드)
docker-compose up -d

# 로그 확인
docker-compose logs -f
```

### 3. 서비스 접속

- **프론트엔드**: http://localhost:3000
- **백엔드 API**: http://localhost:8080
- **Redis Commander**: http://localhost:8081
- **PostgreSQL**: localhost:5432

## 🛠️ 개발 환경 설정

개발 시에는 데이터베이스만 Docker로 실행하고 백엔드/프론트엔드는 로컬에서 실행하는 것을 권장합니다.

```bash
# 데이터베이스만 실행
docker-compose -f docker-compose.dev.yml up -d

# 백엔드 실행 (별도 터미널)
cd back/gpt
./gradlew bootRun

# 프론트엔드 실행 (별도 터미널)
cd front
npm start
```

## 📂 Docker 파일 구조

```
📁 프로젝트 루트/
├── 📄 docker-compose.yml          # 전체 스택 배포용
├── 📄 docker-compose.dev.yml      # 개발 환경용 (DB만)
├── 📄 env.example                 # 환경 변수 예제
├── 📁 back/gpt/
│   ├── 📄 Dockerfile              # Spring Boot 컨테이너
│   └── 📄 application-docker.yml  # Docker 환경 설정
├── 📁 front/
│   ├── 📄 Dockerfile              # React + Nginx 컨테이너
│   └── 📄 nginx.conf              # Nginx 설정
└── 📁 db-init/
    └── 📄 init.sql                # PostgreSQL 초기화
```

## 🔧 Docker Compose 명령어

### 기본 명령어
```bash
# 서비스 시작
docker-compose up -d

# 서비스 중지
docker-compose down

# 서비스 재시작
docker-compose restart

# 빌드 후 시작 (코드 변경 시)
docker-compose up -d --build

# 특정 서비스만 실행
docker-compose up -d postgres redis
```

### 로그 확인
```bash
# 전체 로그
docker-compose logs -f

# 특정 서비스 로그
docker-compose logs -f backend
docker-compose logs -f frontend

# 실시간 로그 (tail)
docker-compose logs -f --tail=100 backend
```

### 컨테이너 관리
```bash
# 컨테이너 상태 확인
docker-compose ps

# 컨테이너 내부 접속
docker-compose exec backend bash
docker-compose exec postgres psql -U postgres -d gpt_clone_dev

# 데이터베이스 초기화
docker-compose exec postgres psql -U postgres -d gpt_clone_dev -f /docker-entrypoint-initdb.d/init.sql
```

## 🗄️ 데이터 관리

### 볼륨 관리
```bash
# 볼륨 목록 확인
docker volume ls

# 볼륨 상세 정보
docker volume inspect chatgpt-clone-coding_postgres_data

# 모든 볼륨 삭제 (주의: 데이터 손실)
docker-compose down -v
```

### 데이터베이스 백업/복원
```bash
# 데이터베이스 백업
docker-compose exec postgres pg_dump -U postgres gpt_clone_dev > backup.sql

# 데이터베이스 복원
docker-compose exec -T postgres psql -U postgres gpt_clone_dev < backup.sql
```

## 🚨 문제 해결

### 일반적인 문제들

#### 1. 포트 충돌
```bash
# 사용 중인 포트 확인
netstat -tulpn | grep :3000
netstat -tulpn | grep :8080

# 포트 변경 (docker-compose.yml 수정)
ports:
  - "3001:3000"  # 3000 대신 3001 사용
```

#### 2. 메모리 부족
```bash
# Docker 메모리 사용량 확인
docker stats

# 불필요한 컨테이너/이미지 정리
docker system prune -a
```

#### 3. 빌드 실패
```bash
# 캐시 없이 다시 빌드
docker-compose build --no-cache

# 개별 서비스 빌드
docker-compose build backend
docker-compose build frontend
```

#### 4. 환경 변수 문제
```bash
# 환경 변수 확인
docker-compose config

# .env 파일 검증
cat .env
```

### 서비스별 헬스체크

#### 백엔드 헬스체크
```bash
curl http://localhost:8080/actuator/health
```

#### 데이터베이스 연결 테스트
```bash
docker-compose exec postgres pg_isready -U postgres
```

#### Redis 연결 테스트
```bash
docker-compose exec redis redis-cli ping
```

## 🔄 배포 시나리오

### 1. 개발 환경
```bash
# 데이터베이스만 Docker
docker-compose -f docker-compose.dev.yml up -d

# 로컬에서 개발
cd back/gpt && ./gradlew bootRun
cd front && npm start
```

### 2. 스테이징 환경
```bash
# 전체 스택 배포
docker-compose up -d

# 로그 모니터링
docker-compose logs -f
```

### 3. 프로덕션 환경
```bash
# 환경 변수 설정 확인
cat .env

# 프로덕션 빌드
docker-compose -f docker-compose.yml up -d --build

# 헬스체크 확인
curl http://localhost:8080/actuator/health
curl http://localhost:3000
```

## 📊 모니터링

### 리소스 모니터링
```bash
# 실시간 리소스 사용량
docker stats

# 컨테이너별 상태
docker-compose top
```

### 로그 분석
```bash
# 에러 로그만 필터링
docker-compose logs backend | grep ERROR

# 특정 시간대 로그
docker-compose logs --since="2024-01-01T10:00:00" backend
```

## 🔒 보안 고려사항

1. **환경 변수**: `.env` 파일을 반드시 `.gitignore`에 추가
2. **데이터베이스**: 프로덕션에서는 강력한 비밀번호 사용
3. **JWT Secret**: 충분히 복잡한 시크릿 키 사용
4. **방화벽**: 필요한 포트만 개방

## 📚 추가 참고 자료

- [Docker Compose 공식 문서](https://docs.docker.com/compose/)
- [Spring Boot Docker 가이드](https://spring.io/guides/topicals/spring-boot-docker/)
- [React Docker 최적화](https://mherman.org/blog/dockerizing-a-react-app/)

---

**문제가 발생하면 GitHub Issues에 문의해주세요!** 🚀 