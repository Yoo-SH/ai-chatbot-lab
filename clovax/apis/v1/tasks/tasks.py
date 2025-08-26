from fastapi import APIRouter, HTTPException, Path
from schemas.request.chat_completions_request import ChatRequest
from schemas.response.chat_completions_response import ChatResponse

router = APIRouter()

@router.post("/{task_id}/chat-completions", response_model=ChatResponse, tags=["tasks"])
async def chat_completion_with_task(
    chat_request: ChatRequest,
    task_id: str = Path(..., description="튜닝 학습한 작업 ID")
):
    """
    CLOVA Studio Task-based Chat Completions API
    
    튜닝 학습한 작업을 사용하여 채팅 완성을 수행합니다.
    
    **제한사항:**
    - 이미지 입력: 지원하지 않음
    - 추론: 지원하지 않음
    - Function calling: 지원하지 않음
    - Structured Outputs: 지원하지 않음
    """
    try:
        # 요청 검증
        if not chat_request.messages:
            raise HTTPException(
                status_code=400,
                detail="메시지가 비어있습니다."
            )
        
        # system 메시지 개수 검증
        system_messages = [msg for msg in chat_request.messages if msg.role.value == "system"]
        if len(system_messages) > 1:
            raise HTTPException(
                status_code=400,
                detail="system 메시지는 요청당 1개만 포함할 수 있습니다."
            )
        
        # 이미지 입력 검증 (지원하지 않음)
        for msg in chat_request.messages:
            if isinstance(msg.content, list):
                for item in msg.content:
                    if item.type.value == "image_url":
                        raise HTTPException(
                            status_code=400,
                            detail="Task API는 이미지 입력을 지원하지 않습니다."
                        )
        
        response = await clova_service.chat_completion_with_task(chat_request, task_id)
        return response
        
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"Task 기반 채팅 완성 처리 중 오류가 발생했습니다: {str(e)}"
        )
