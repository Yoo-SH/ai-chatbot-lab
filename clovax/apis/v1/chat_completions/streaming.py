from fastapi import APIRouter, HTTPException, Path
from fastapi.responses import StreamingResponse
from schemas.chat import ChatRequest
from services.clova_service import ClovaService
import json

router = APIRouter()
clova_service = ClovaService()

@router.post("/{model_name}/stream", tags=["chat-completions"])
async def streaming_chat_completion(
    chat_request: ChatRequest,
    model_name: str = Path(..., description="모델 이름 (예: HCX-005, HCX-DASH-002)")
):
    """
    스트리밍 채팅 완성 API
    
    **모델별 제한사항:**
    - HCX-005: 일반 모델, 이미지 입력 지원, 최대 128,000 토큰
    - HCX-DASH-002: 일반 모델, 텍스트 전용, 최대 32,000 토큰
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

        # 메시지 변환 (문자열과 객체 배열 모두 지원)
        messages = []
        for message in chat_request.messages:
            if isinstance(message.content, str):
                # 문자열 content인 경우
                messages.append({
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
                
                messages.append({
                    "role": message.role,
                    "content": content_array
                })

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

        # CLOVA Studio API 스트리밍 호출
        async def generate_stream():
            try:
                # 테스트용 초기 응답
                yield "data: {\"status\": \"streaming_started\", \"message\": \"스트리밍 시작\"}\n\n"
                
                async for chunk in clova_service.streaming_chat_completion(model_name, clova_request):
                    # chunk가 문자열인지 확인하고 SSE 형식으로 전달
                    if isinstance(chunk, str):
                        yield chunk
                    else:
                        # dict인 경우 JSON으로 변환
                        yield f"data: {json.dumps(chunk, ensure_ascii=False)}\n\n"
                
                # 스트리밍 완료 신호
                yield "data: {\"status\": \"streaming_completed\", \"message\": \"스트리밍 완료\"}\n\n"
                
            except Exception as e:
                # 에러 발생 시 에러 메시지 전달
                error_data = {
                    "error": str(e),
                    "status": "error"
                }
                yield f"data: {json.dumps(error_data, ensure_ascii=False)}\n\n"

        return StreamingResponse(
            generate_stream(),
            media_type="text/event-stream",
            headers={
                "Cache-Control": "no-cache", 
                "Connection": "keep-alive"
            }
        )

    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"스트리밍 응답 처리 중 오류가 발생했습니다: {str(e)}"
        )

