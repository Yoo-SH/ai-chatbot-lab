# 멀티스테이지 빌드를 위한 첫 번째 스테이지 - 빌드 환경
FROM node:18-alpine AS build

# 작업 디렉토리 설정
WORKDIR /app

# package.json과 package-lock.json 복사 (캐시 최적화)
COPY package*.json ./

# 의존성 설치
RUN npm ci --only=production

# 소스 코드 복사
COPY . .

# React 앱 빌드
RUN npm run build

# 두 번째 스테이지 - 프로덕션 환경 (Nginx)
FROM nginx:alpine

# Nginx 설정 파일 복사
COPY nginx.conf /etc/nginx/nginx.conf

# 빌드된 React 앱을 Nginx 웹 루트로 복사
COPY --from=build /app/build /usr/share/nginx/html

# 포트 노출
EXPOSE 3000

# Nginx 실행
CMD ["nginx", "-g", "daemon off;"] 