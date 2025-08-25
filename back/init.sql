-- 데이터베이스가 없으면 생성
-- CREATE DATABASE IF NOT EXISTS chatgpt_clone;

-- 확장 모듈 설치 (UUID 생성용)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 한국어 지원을 위한 로케일 설정
-- SET lc_messages = 'ko_KR.UTF-8';
-- SET lc_monetary = 'ko_KR.UTF-8';
-- SET lc_numeric = 'ko_KR.UTF-8';
-- SET lc_time = 'ko_KR.UTF-8';

-- 기본 사용자 생성 (개발용)
-- INSERT INTO users (name, email, password, created_at, updated_at) 
-- VALUES ('관리자', 'admin@chatgpt.com', '$2a$10$encoded_password', NOW(), NOW())
-- ON CONFLICT (email) DO NOTHING; 