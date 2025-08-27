from fastapi import APIRouter, HTTPException, Path
from schemas.request.chat_completions_request import ChatRequest
from schemas.response.chat_completions_response import ChatResponse
from services.clova_chat_service import ClovaService
from services.chat_memory_service import chat_memory_service
from services.rag_retrieval_service import rag_retrieval_service, RetrievalConfig
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
                    "sessionId": "550e8400-e29b-41d4-a716-446655440000",
                    "ragUsed": False
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
    
    **RAG (검색 증강 생성) 기능:**
    - useRAG: true 설정 시 벡터 DB에서 관련 문서 검색
    - ragTopK: 검색할 문서 개수 (기본값: 3, 범위: 1-10)
    - ragThreshold: 유사도 임계값 (기본값: 0.1, 범위: 0.0-1.0)
    - 검색된 문서를 바탕으로 정확한 답변 제공
    
    **사용법:**
    1. 첫 번째 요청: sessionId 없이 전송
    2. 응답에서 sessionId 확인
    3. 이후 요청: sessionId 포함하여 전송
    4. RAG 사용 시: useRAG=true로 설정
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

        # RAG 검색 수행 (useRAG가 True인 경우)
        rag_context = ""
        if chat_request.useRAG:
            # 사용자 메시지에서 검색 쿼리 추출
            user_messages = [msg for msg in current_messages if msg.get("role") == "user"]
            if user_messages:
                last_user_message = user_messages[-1].get("content", "")
                if isinstance(last_user_message, list):
                    # 배열 형태의 content에서 텍스트만 추출
                    text_parts = [
                        item.get("text", "") 
                        for item in last_user_message 
                        if isinstance(item, dict) and item.get("type") == "text"
                    ]
                    query = " ".join(text_parts)
                else:
                    query = last_user_message
                
                # RAG 검색 수행
                if query.strip():
                    retrieval_config = RetrievalConfig(
                        top_k=chat_request.ragTopK or 3,
                        similarity_threshold=chat_request.ragThreshold or 0.1
                    )
                    
                    search_results = await rag_retrieval_service.search_documents_async(
                        query=query,
                        config=retrieval_config
                    )
                    
                    # 검색 결과를 컨텍스트로 변환
                    if search_results:
                        rag_context = rag_retrieval_service._create_context_from_results(
                            search_results, max_length=2000
                        )

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
            
        # RAG 컨텍스트가 있으면 기존 시스템 메시지에 통합
        if rag_context:
            rag_instruction = f"""

[참고 문서]
{rag_context}

위 참고 문서를 바탕으로 사용자의 질문에 답변해주세요. 참고 문서의 내용을 바탕으로 정확한 정보를 제공하고, 참고 문서에 없는 내용은 추측하지 마세요. 답변 끝에 "※ 제공된 문서를 바탕으로 작성된 답변입니다."를 추가하세요."""
            
            # 기존 시스템 메시지를 찾아서 수정
            system_message_found = False
            for i, msg in enumerate(messages):
                if msg.get("role") == "system":
                    # 기존 시스템 메시지에 RAG 컨텍스트 추가
                    if isinstance(msg["content"], str):
                        messages[i]["content"] = msg["content"] + rag_instruction
                    elif isinstance(msg["content"], list):
                        # content가 배열인 경우 텍스트 부분에 추가
                        for content_item in msg["content"]:
                            if isinstance(content_item, dict) and content_item.get("type") == "text":
                                content_item["text"] = content_item["text"] + rag_instruction
                                break
                    system_message_found = True
                    break
            
            # 시스템 메시지가 없으면 새로 추가
            if not system_message_found:
                rag_system_message = {
                    "role": "system",
                    "content": f"친절하게 답변하는 AI 어시스턴트입니다.{rag_instruction}"
                }
                messages.insert(0, rag_system_message)

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
        
        # 응답에 sessionId와 RAG 사용 여부 추가
        response["sessionId"] = session_id
        response["ragUsed"] = bool(chat_request.useRAG and rag_context)
        
        return response

    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"응답 처리 중 오류가 발생했습니다: {str(e)}"
        )

