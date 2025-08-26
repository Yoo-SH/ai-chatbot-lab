from pydantic import BaseModel
from typing import Optional
import os
from dotenv import load_dotenv

load_dotenv()


class Settings(BaseModel):
    # CLOVA Studio API 키 및 기본 URL
    CLOVA_STUDIO_API_KEY: str = os.getenv("CLOVA_STUDIO_API_KEY", "")
    CLOVA_STUDIO_BASE_URL: str = os.getenv("CLOVA_STUDIO_BASE_URL", "https://clovastudio.stream.ntruss.com")
    
    # 서버 설정
    HOST: str = os.getenv("HOST", "0.0.0.0")
    PORT: int = int(os.getenv("PORT", "8000"))
    RELOAD: bool = os.getenv("RELOAD", "false").lower() == "true"
    DEBUG: bool = os.getenv("DEBUG", "false").lower() == "true"
    
    # API 버전 및 프로젝트 설정
    API_V1_STR: str = "/api/v1"
    PROJECT_NAME: str = "CLOVAX API"
    VERSION: str = "1.0.0"
    
    class Config:
        env_file = ".env"
        case_sensitive = True

settings = Settings()
