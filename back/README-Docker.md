# 🐳 Docker를 사용한 개발 환경 설정

## 📋 포함된 서비스

- **PostgreSQL** (포트: 5432) - 메인 데이터베이스
- **Redis** (포트: 6379) - JWT 토큰 캐시
- **Redis Commander** (포트: 8081) - Redis 관리 도구
- **pgAdmin** (포트: 8082) - PostgreSQL 관리 도구

## 🚀 실행 방법

### 1. Docker 서비스 시작
```bash
cd back
docker-compose up -d
```

### 2. 로그 확인
```bash
docker-compose logs -f
```

### 3. 특정 서비스만 시작
```bash
# Redis만 시작
docker-compose up -d redis

# PostgreSQL만 시작  
docker-compose up -d postgres
```

### 4. 서비스 중지
```bash
docker-compose down
```

### 5. 볼륨까지 삭제 (데이터 초기화)
```bash
docker-compose down -v
```

## 🔧 접속 정보

### PostgreSQL
- **Host**: localhost
- **Port**: 5432
- **Database**: chatgpt_clone
- **Username**: postgres
- **Password**: password

### Redis
- **Host**: localhost
- **Port**: 6379
- **Password**: 없음

### 관리 도구 접속

#### pgAdmin (PostgreSQL 관리)
- **URL**: http://localhost:8082
- **Email**: admin@example.com
- **Password**: admin

#### Redis Commander (Redis 관리)
- **URL**: http://localhost:8081

## 📝 Spring Boot 설정

`application-dev.yml`에서 다음 설정 사용:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/chatgpt_clone
    username: postgres
    password: password
  
  data:
    redis:
      host: localhost
      port: 6379
```

## 🛠️ 유용한 명령어

```bash
# 컨테이너 상태 확인
docker-compose ps

# Redis CLI 접속
docker exec -it gpt-redis redis-cli

# PostgreSQL CLI 접속
docker exec -it gpt-postgres psql -U postgres -d chatgpt_clone

# 컨테이너 재시작
docker-compose restart redis
docker-compose restart postgres
```

## 🔄 데이터 백업/복원

### PostgreSQL 백업
```bash
docker exec gpt-postgres pg_dump -U postgres chatgpt_clone > backup.sql
```

### PostgreSQL 복원
```bash
docker exec -i gpt-postgres psql -U postgres chatgpt_clone < backup.sql
``` 