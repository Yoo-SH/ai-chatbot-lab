from pydantic import BaseModel, Field
from typing import List, Optional

class ModelCapability(BaseModel):
    """모델 능력 정보"""
    text: bool = Field(..., description="텍스트 처리 지원")
    image: bool = Field(False, description="이미지 처리 지원")
    vision: bool = Field(False, description="비전 처리 지원")
    inference: bool = Field(False, description="추론 처리 지원")

class ModelInfo(BaseModel):
    """개별 모델 정보"""
    id: str = Field(..., description="모델 ID")
    name: str = Field(..., description="모델 이름")
    description: str = Field(..., description="모델 설명")
    capabilities: List[str] = Field(..., description="지원하는 기능들")
    max_tokens: Optional[int] = Field(None, description="최대 토큰 수 (일반 모델)")
    max_completion_tokens: Optional[int] = Field(None, description="최대 완성 토큰 수 (추론 모델)")
    supported_features: List[str] = Field(..., description="지원하는 기능들")
    unsupported_features: List[str] = Field(..., description="지원하지 않는 기능들")
    model_type: str = Field(..., description="모델 타입 (general/inference)")

class ModelTypeNotes(BaseModel):
    """모델 타입별 설명"""
    general: str = Field(..., description="일반 모델 사용법")
    inference: str = Field(..., description="추론 모델 사용법")

class ModelsResponse(BaseModel):
    """모델 목록 응답"""
    models: List[ModelInfo] = Field(..., description="사용 가능한 모델 목록")
    global_limitations: List[str] = Field(..., description="전역 제한사항")
    model_type_notes: ModelTypeNotes = Field(..., description="모델 타입별 사용법")

    model_config = {
        "json_schema_extra": {
            "example": {
                "models": [
                    {
                        "id": "HCX-005",
                        "name": "HCX-005",
                        "description": "일반 모델, 이미지 입력 지원, 최대 128,000 토큰",
                        "capabilities": ["text", "image", "vision"],
                        "max_tokens": 128000,
                        "max_completion_tokens": None,
                        "supported_features": ["이미지 입력", "AI 필터"],
                        "unsupported_features": ["추론", "Function Calling", "Structured Outputs"],
                        "model_type": "general"
                    },
                    {
                        "id": "HCX-007",
                        "name": "HCX-007",
                        "description": "추론 모델, 이미지 입력 지원, 최대 128,000 토큰",
                        "capabilities": ["text", "image", "vision", "inference"],
                        "max_tokens": None,
                        "max_completion_tokens": 4096,
                        "supported_features": ["이미지 입력", "추론", "AI 필터"],
                        "unsupported_features": ["Function Calling", "Structured Outputs", "stop 파라미터"],
                        "model_type": "inference"
                    }
                ],
                "global_limitations": [
                    "Function Calling 미지원",
                    "Structured Outputs 미지원",
                    "Task API는 이미지 입력 미지원"
                ],
                "model_type_notes": {
                    "general": "maxTokens 사용, stop 파라미터 사용 가능",
                    "inference": "maxCompletionTokens 사용, stop 파라미터 사용 불가"
                }
            }
        }
    }
