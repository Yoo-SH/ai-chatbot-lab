from fastapi import APIRouter
from schemas.models import ModelsResponse

router = APIRouter()

@router.get("/", response_model=ModelsResponse, tags=["models"])
async def get_available_models():
    """
    사용 가능한 모델 목록을 반환합니다.
    
    **모델별 제한사항:**
    - HCX-005: 일반 모델, 이미지 입력 지원, 최대 128,000 토큰
    - HCX-DASH-002: 일반 모델, 텍스트 전용, 최대 32,000 토큰
    - 모든 모델: Function Calling, Structured Outputs 미지원
    """
    return {
        "models": [
            {
                "id": "HCX-005",
                "name": "HCX-005",
                "description": "일반 모델, 이미지 입력 지원, 최대 128,000 토큰",
                "capabilities": ["text", "image", "vision"],
                "max_tokens": 128000,
                "max_completion_tokens": None,
                "supported_features": ["이미지 입력", "AI 필터"],
                "unsupported_features": ["Function Calling", "Structured Outputs"],
                "model_type": "general"
            },

            {
                "id": "HCX-DASH-002",
                "name": "HCX-DASH-002", 
                "description": "일반 모델, 텍스트 전용, 최대 32,000 토큰",
                "capabilities": ["text"],
                "max_tokens": 32000,
                "max_completion_tokens": None,
                "supported_features": ["빠른 응답", "AI 필터"],
                "unsupported_features": ["이미지 입력", "Function Calling", "Structured Outputs"],
                "model_type": "general"
            }
        ],
        "global_limitations": [
            "Function Calling 미지원",
            "Structured Outputs 미지원",
            "Task API는 이미지 입력 미지원"
        ],
        "model_type_notes": {
            "general": "maxTokens 또는 maxCompletionTokens 사용 가능, stop 파라미터 사용 가능"
        }
    }
