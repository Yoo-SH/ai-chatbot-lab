from fastapi import APIRouter, HTTPException, Path
from schemas.request.chat_completions_request import ChatRequest
from schemas.response.chat_completions_response import ChatResponse
from services.clova_chat_service import ClovaService
from services.chat_memory_service import chat_memory_service
import uuid

router = APIRouter()
clova_service = ClovaService()

@router.post("/{model_name}", tags=["chat-completions"], response_model=ChatResponse, responses={
    200: {
        "description": "채팅 완성 응답 (sessionId 포함)",
        "model": ChatResponse,
        "content": {
            "application/json": {
                "example": {
                    "status": {
                        "code": "20000",
                        "message": "OK"
                    },
                    "result": {
                        "message": {
                            "role": "assistant",
                            "content": "안녕하세요! 무엇을 도와드릴까요?"
                        },
                        "finishReason": "stop",
                        "created": 1756173882,
                        "usage": {
                            "promptTokens": 10,
                            "completionTokens": 15,
                            "totalTokens": 25
                        }
                    },
                    "sessionId": "550e8400-e29b-41d4-a716-446655440000"
                }
            }
        }
    },
    400: {
        "description": "잘못된 요청",
        "content": {
            "application/json": {
                "examples": {
                    "unsupported_model": {
                        "summary": "지원하지 않는 모델",
                        "value": {
                            "detail": "지원하지 않는 모델입니다. 지원 모델: HCX-005, HCX-DASH-002"
                        }
                    },
                    "image_not_supported": {
                        "summary": "이미지 미지원 모델",
                        "value": {
                            "detail": "HCX-DASH-002은 이미지 입력을 지원하지 않습니다"
                        }
                    }
                }
            }
        }
    }
})

async def chat_completion(
    chat_request: ChatRequest,
    model_name: str = Path(..., description="모델 이름 (예: HCX-005, HCX-DASH-002)")
):
    """
    채팅 완성 API
    
    **모델별 제한사항:**
    - HCX-005: 일반 모델, 이미지 입력 지원, 최대 128,000 토큰
    - HCX-DASH-002: 일반 모델, 텍스트 전용, 최대 32,000 토큰
    
    **멀티턴 대화:**
    - sessionId 없이 요청: 단일턴 (독립적인 대화)
    - sessionId와 함께 요청: 멀티턴 (이전 대화 기억)
    - 응답에 sessionId가 포함되어 다음 요청에서 사용 가능
    
    **사용법:**
    1. 첫 번째 요청: sessionId 없이 전송
    2. 응답에서 sessionId 확인
    3. 이후 요청: sessionId 포함하여 전송
    """
    try:
        # 지원 모델 검증
        if model_name not in ["HCX-005", "HCX-DASH-002"]:
            raise HTTPException(
                status_code=400,
                detail=f"지원하지 않는 모델입니다. 지원 모델: HCX-005, HCX-DASH-002"
            )
        
        # maxTokens와 maxCompletionTokens 동시 사용 검증
        if chat_request.maxTokens and chat_request.maxCompletionTokens:
            raise HTTPException(
                status_code=400,
                detail="maxTokens와 maxCompletionTokens는 동시에 사용할 수 없습니다"
            )

        # 이미지 개수 검증 (HCX-005만 이미지 지원)
        if model_name == "HCX-005":
            image_count = 0
            total_images = 0
            for message in chat_request.messages:
                if isinstance(message.content, list):
                    for content in message.content:
                        if isinstance(content, dict) and content.get("type") == "image_url":
                            image_count += 1
                            total_images += 1
            
            # 턴당 이미지 1개, 요청당 최대 5개 제한
            if image_count > 1:
                raise HTTPException(
                    status_code=400,
                    detail="HCX-005에서는 턴당 최대 1개의 이미지만 입력할 수 있습니다"
                )
            if total_images > 5:
                raise HTTPException(
                    status_code=400,
                    detail="HCX-005에서는 요청당 최대 5개의 이미지만 입력할 수 있습니다"
                )
        else:
            # HCX-DASH-002와 다른 모델들은 이미지 미지원
            for message in chat_request.messages:
                if isinstance(message.content, list):
                    for content in message.content:
                        if isinstance(content, dict) and content.get("type") == "image_url":
                            raise HTTPException(
                                status_code=400,
                                detail=f"{model_name}은 이미지 입력을 지원하지 않습니다"
                            )

        # 세션 ID 처리 (없으면 자동 생성)
        session_id = chat_request.sessionId or str(uuid.uuid4())
        
        # 메시지 변환 (문자열과 객체 배열 모두 지원)
        current_messages = []
        for message in chat_request.messages:
            if isinstance(message.content, str):
                # 문자열 content인 경우
                current_messages.append({
                    "role": message.role,
                    "content": message.content
                })
            else:
                # 배열 content인 경우
                content_array = []
                for content in message.content:
                    if isinstance(content, dict):
                        content_array.append(content)
                    else:
                        # Pydantic 모델인 경우 dict로 변환
                        content_array.append(content.dict() if hasattr(content, 'dict') else content)
                
                current_messages.append({
                    "role": message.role,
                    "content": content_array
                })

        # 멀티턴 지원: 이전 대화 내역과 현재 메시지 결합
        if chat_request.sessionId:
            messages = chat_memory_service.get_messages_for_clovax(
                session_id=session_id,
                current_messages=current_messages,
                memory_type=chat_request.memoryType or "buffer_window"
            )
        else:
            # 세션 ID가 없으면 현재 메시지만 사용 (기존 방식)
            messages = current_messages

        # CLOVA Studio API 요청 데이터 준비 (JSON 직렬화 가능한 형태로 변환)
        clova_request = {
            "messages": messages,
            "topP": chat_request.topP,
            "topK": chat_request.topK,
            "temperature": chat_request.temperature,
            "repetitionPenalty": chat_request.repetitionPenalty,
            "stop": chat_request.stop if chat_request.stop else [],
            "includeAiFilters": chat_request.includeAiFilters
        }

        # 토큰 파라미터 설정
        if chat_request.maxTokens:
            clova_request["maxTokens"] = chat_request.maxTokens
        elif chat_request.maxCompletionTokens:
            clova_request["maxCompletionTokens"] = chat_request.maxCompletionTokens

        # seed 설정 (제공된 경우에만)
        if chat_request.seed is not None:
            clova_request["seed"] = chat_request.seed

        # CLOVA Studio API 호출
        response = await clova_service.chat_completion(model_name, clova_request)
        
        # 멀티턴 지원: 대화 내용을 메모리에 저장
        if response.get("result", {}).get("message", {}).get("content"):
            # 현재 사용자 메시지와 AI 응답을 메모리에 저장
            user_messages = [msg for msg in current_messages if msg.get("role") == "user"]
            if user_messages:
                # 마지막 사용자 메시지 추출
                last_user_message = user_messages[-1].get("content", "")
                if isinstance(last_user_message, list):
                    # 배열 형태의 content에서 텍스트만 추출
                    text_parts = [
                        item.get("text", "") 
                        for item in last_user_message 
                        if isinstance(item, dict) and item.get("type") == "text"
                    ]
                    last_user_message = " ".join(text_parts)
                
                ai_response = response.get("result", {}).get("message", {}).get("content", "")
                
                # 메모리에 대화 저장
                chat_memory_service.add_message_to_memory(
                    session_id=session_id,
                    user_message=last_user_message,
                    assistant_message=ai_response,
                    memory_type=chat_request.memoryType or "buffer_window"
                )
        
        # 응답에 sessionId 추가
        response["sessionId"] = session_id
        
        return response

    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"응답 처리 중 오류가 발생했습니다: {str(e)}"
        )

