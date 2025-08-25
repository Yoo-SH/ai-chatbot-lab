# Python 베이스 이미지
FROM python:3.11-slim

# 루트 작업 디렉토리 설정 (ai 패키지의 부모 디렉토리)
WORKDIR /workspace

# 시스템 패키지 업데이트 및 필요한 도구 설치
RUN apt-get update && apt-get install -y \
    curl \
    && rm -rf /var/lib/apt/lists/*

# requirements.txt 복사 및 의존성 설치 (캐시 최적화)
COPY requirements.txt ./ai/
RUN pip install --no-cache-dir -r ./ai/requirements.txt

# ai 패키지 전체 복사
COPY . ./ai/

# 애플리케이션 실행을 위한 사용자 생성
RUN addgroup --system fastapi && adduser --system fastapi --ingroup fastapi
RUN chown -R fastapi:fastapi /workspace
USER fastapi:fastapi

# 포트 노출
EXPOSE 8000

# 헬스체크 설정
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:8000/health || exit 1

# 환경 변수 설정 - ai 패키지를 인식할 수 있도록 설정
ENV PYTHONPATH=/workspace
ENV PYTHONUNBUFFERED=1

# FastAPI 애플리케이션 실행 - 이제 ai가 최상위 패키지로 인식됨
CMD ["uvicorn", "ai.ai_api.api_server:app", "--host", "0.0.0.0", "--port", "8000"] 