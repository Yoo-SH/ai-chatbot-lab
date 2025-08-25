# 🔴 Redis 전용 Docker 설정

Backend는 윈도우 환경에서 실행하고, Redis만 Docker로 운영하기 위한 설정입니다.

## 🚀 Redis 실행 방법

### 방법 1: Docker Compose 사용 (권장)
```bash
cd back
docker-compose -f docker-compose-redis.yml up -d
```

### 방법 2: Docker 명령어 직접 사용
```bash
# Redis 컨테이너 실행
docker run -d \
  --name gpt-redis-only \
  -p 6379:6379 \
  -v redis_data:/data \
  redis:7-alpine redis-server --appendonly yes

# Redis Commander 실행 (선택사항)
docker run -d \
  --name gpt-redis-commander \
  --link gpt-redis-only:redis \
  -p 8081:8081 \
  -e REDIS_HOSTS=local:redis:6379 \
  rediscommander/redis-commander:latest
```

## 🔧 Redis 관리 명령어

### 상태 확인
```bash
# 컨테이너 상태 확인
docker ps | grep redis

# Redis 연결 테스트
docker exec -it gpt-redis-only redis-cli ping
```

### Redis CLI 접속
```bash
docker exec -it gpt-redis-only redis-cli
```

### 로그 확인
```bash
docker logs gpt-redis-only
```

### Redis 중지/시작
```bash
# 중지
docker-compose -f docker-compose-redis.yml down

# 시작
docker-compose -f docker-compose-redis.yml up -d
```

## 🌐 접속 정보

### Redis 서버
- **Host**: localhost
- **Port**: 6379
- **Password**: 없음

### Redis Commander (웹 관리 도구)
- **URL**: http://localhost:8081

## ⚙️ Spring Boot 설정

Backend에서 Redis 연결을 위한 `application.yml` 설정:

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      timeout: 2000ms
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 0
```

## 🔄 유용한 명령어

```bash
# Redis 키 목록 조회
docker exec -it gpt-redis-only redis-cli keys "*"

# 특정 키 값 조회
docker exec -it gpt-redis-only redis-cli get "key_name"

# Redis 메모리 사용량 확인
docker exec -it gpt-redis-only redis-cli info memory

# Redis 설정 확인
docker exec -it gpt-redis-only redis-cli config get "*"
``` 