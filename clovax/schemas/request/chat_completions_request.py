from pydantic import BaseModel, Field
from typing import List, Union, Optional

class ImageUrl(BaseModel):
    """이미지 URL 정보"""
    url: str = Field(..., description="파일 확장자를 포함한 단일 이미지의 공개 URL")

class DataUri(BaseModel):
    """Base64 이미지 데이터"""
    data: str = Field(..., description="Base64로 인코딩된 이미지 문자열")

class TextContent(BaseModel):
    """텍스트 콘텐츠"""
    type: str = Field("text", description="콘텐츠 타입")
    text: str = Field(..., description="텍스트 내용")

class ImageContent(BaseModel):
    """이미지 콘텐츠"""
    type: str = Field("image_url", description="콘텐츠 타입")
    imageUrl: Optional[ImageUrl] = Field(None, description="이미지 URL 정보")
    dataUri: Optional[DataUri] = Field(None, description="Base64 이미지 데이터")

ContentItem = Union[TextContent, ImageContent, dict]

class Message(BaseModel):
    """채팅 메시지"""
    role: str = Field(..., description="메시지 역할 (system, user, assistant)")
    content: Union[str, List[ContentItem]] = Field(..., description="메시지 콘텐츠 (텍스트 문자열 또는 content 객체 배열)")

class ChatRequest(BaseModel):
    """채팅 요청"""
    messages: List[Message] = Field(..., description="메시지 목록")
    requestId: Optional[str] = Field(None, description="요청 ID")
    sessionId: Optional[str] = Field(None, description="세션 ID (멀티턴 대화용)")
    memoryType: Optional[str] = Field("buffer_window", description="메모리 타입 (buffer_window, token_buffer)")
    memoryK: Optional[int] = Field(10, description="메모리 윈도우 크기")
    topP: Optional[float] = Field(0.8, description="Top-p 샘플링 값")
    topK: Optional[int] = Field(0, description="Top-k 샘플링 값")
    maxTokens: Optional[int] = Field(None, description="최대 토큰 수 (일반 모델용)")
    maxCompletionTokens: Optional[int] = Field(None, description="최대 완성 토큰 수 (추론 모델용)")
    temperature: Optional[float] = Field(0.5, description="온도 값")
    repetitionPenalty: Optional[float] = Field(1.1, description="반복 패널티")
    stop: Optional[List[str]] = Field([], description="중단 토큰 목록")
    seed: Optional[int] = Field(None, description="시드 값")
    includeAiFilters: Optional[bool] = Field(True, description="AI 필터 포함 여부")

    model_config = {
        "json_schema_extra": {
            "example": {
                "messages": [
                    {
                        "role": "system",
                        "content": [
                            {
                                "type": "text",
                                "text": "친절하게 답변하는 AI 어시스턴트입니다."
                            }
                        ]
                    },
                    {
                        "role": "user",
                        "content": [
                            {
                                "type": "image_url",
                                "imageUrl": {
                                    "url": "https://cdn.kormedi.com/wp-content/uploads/2023/07/ck-pc003052084-l.jpg.webp"
                                }
                            },
                            {
                                "type": "text",
                                "text": "이 사진에 대해서 설명해줘"
                            }
                        ]
                    }
                ],
                "topP": 0.8,
                "topK": 0,
                "maxTokens": 100,
                "temperature": 0.5,
                "repetitionPenalty": 1.1,
                "stop": []
                
            }
        }
    }
