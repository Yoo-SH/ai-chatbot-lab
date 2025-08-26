from pydantic import BaseModel, Field
from typing import List, Optional

# CLOVA Studio 응답 스키마
class Status(BaseModel):
    """응답 상태"""
    code: str = Field(..., description="응답 상태 코드", example="20000")
    message: str = Field(..., description="응답 상태 메시지", example="OK")

class Usage(BaseModel):
    """토큰 사용량"""
    completionTokens: int = Field(..., description="생성 토큰 수", example=100)
    promptTokens: int = Field(..., description="입력(프롬프트) 토큰 수", example=1694)
    totalTokens: int = Field(..., description="전체 토큰 수", example=1794)

class ResponseMessage(BaseModel):
    """응답 메시지"""
    role: str = Field(..., description="메시지 역할 (assistant)", example="assistant")
    content: str = Field(..., description="메시지 내용", example="사진에는 신선하고 맛있어 보이는 복숭아들이 하얀 배경 위에 놓여 있습니다...")

class AIFilter(BaseModel):
    """AI 필터 결과"""
    groupName: str = Field(..., description="AI 필터 카테고리", example="curse")
    name: str = Field(..., description="AI 필터 세부 카테고리", example="insult")
    score: str = Field(..., description="AI 필터 점수", example="2")
    result: Optional[str] = Field(None, description="AI 필터 정상 작동 여부", example="OK")

class Result(BaseModel):
    """응답 결과"""
    created: int = Field(..., description="응답 날짜 (Unix timestamp seconds)", example=1756173882)
    usage: Usage = Field(..., description="토큰 사용량")
    message: ResponseMessage = Field(..., description="대화 메시지")
    finishReason: str = Field(..., description="토큰 생성 중단 이유", example="length")
    seed: int = Field(..., description="입력 seed 값", example=3464118315)
    aiFilter: Optional[List[AIFilter]] = Field(None, description="AI 필터 결과")

class ChatResponse(BaseModel):
    """채팅 응답"""
    status: Status = Field(..., description="응답 상태")
    result: Result = Field(..., description="응답 결과")
    
    model_config = {
        "json_schema_extra": {
            "example": {
                "status": {
                    "code": "20000",
                    "message": "OK"
                },
                "result": {
                    "message": {
                        "role": "assistant",
                        "content": "사진에는 신선하고 맛있어 보이는 복숭아들이 하얀 배경 위에 놓여 있습니다. 이 과일은 선명한 붉은색 껍질과 안쪽의 부드럽고 황색을 띠는 살로 구성되어 있으며, 일부는 반으로 잘려 있어 내부의 모습을 볼 수 있습니다.\n\n복숭아는 여름철 대표적인 과일 중 하나로 상큼하고 달콤한 맛이 특징이며 비타민 C와 A가 풍부하며 섬유질이 많아 소화를 돕고 변비 예방에도 효과적입니다."
                    },
                    "finishReason": "length",
                    "created": 1756173882,
                    "seed": 3464118315,
                    "usage": {
                        "promptTokens": 1694,
                        "completionTokens": 100,
                        "totalTokens": 1794
                    },
                    "aiFilter": [
                        {
                            "groupName": "curse",
                            "name": "insult",
                            "score": "2",
                            "result": "OK"
                        },
                        {
                            "groupName": "curse",
                            "name": "discrimination",
                            "score": "2",
                            "result": "OK"
                        },
                        {
                            "groupName": "unsafeContents",
                            "name": "sexualHarassment",
                            "score": "2",
                            "result": "OK"
                        }
                    ]
                }
            }
        },
        "field_order": ["status", "result"],  # 필드 순서 명시
        "populate_by_name": True
    }


# 스트리밍 응답 스키마
class StreamTokenEvent(BaseModel):
    """스트리밍 토큰 이벤트"""
    created: int = Field(..., description="응답 시간 타임스탬프", example=1756173882)
    usage: Optional[Usage] = Field(None, description="토큰 사용량")
    message: ResponseMessage = Field(..., description="대화 메시지")
    finishReason: Optional[str] = Field(None, description="토큰 생성 중단 이유", example="length")

class StreamResultEvent(BaseModel):
    """스트리밍 결과 이벤트"""
    created: int = Field(..., description="응답 시간 타임스탬프", example=1756173882)
    usage: Usage = Field(..., description="토큰 사용량")
    message: ResponseMessage = Field(..., description="대화 메시지")
    finishReason: str = Field(..., description="토큰 생성 중단 이유", example="length")
    aiFilter: Optional[List[AIFilter]] = Field(None, description="AI 필터 결과")

class ErrorEvent(BaseModel):
    """에러 이벤트"""
    status: Status = Field(..., description="응답 상태")
    
    class Config:
        schema_extra = {
            "example": {
                "status": {
                    "code": "40000",
                    "message": "Bad Request"
                }
            }
        }

class SignalEvent(BaseModel):
    """시그널 이벤트"""
    data: str = Field(..., description="전달할 시그널 데이터 정보", example="stream_end")
