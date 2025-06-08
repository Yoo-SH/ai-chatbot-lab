-- ChatGPT Clone Database 초기화 스크립트
-- 데이터베이스가 이미 존재하는지 확인하고 생성
SELECT 'CREATE DATABASE gpt_clone_dev'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'gpt_clone_dev')\gexec

-- 데이터베이스 연결
\c gpt_clone_dev;

-- 기본 확장 기능 활성화
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- 기본 스키마 권한 설정
GRANT ALL PRIVILEGES ON DATABASE gpt_clone_dev TO postgres;
GRANT ALL PRIVILEGES ON SCHEMA public TO postgres;

-- 시퀀스 권한 설정
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO postgres;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO postgres;

-- 성능 최적화를 위한 인덱스 (JPA가 테이블을 생성한 후 적용됨)
-- 실제 운영에서는 애플리케이션 시작 후 수동으로 적용하거나 별도 마이그레이션 스크립트 사용

-- 로그 출력
\echo '데이터베이스 초기화가 완료되었습니다.'
\echo 'gpt_clone_dev 데이터베이스가 준비되었습니다.' 