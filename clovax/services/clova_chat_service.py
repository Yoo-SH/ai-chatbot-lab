import requests
import json
import uuid
import time
from typing import Dict, Any, Optional, AsyncGenerator
from fastapi import HTTPException
from core.config import settings
from schemas.request.chat_completions_request import ChatRequest

class ClovaService:
    def __init__(self):
        self.api_key = settings.CLOVA_STUDIO_API_KEY
        self.base_url = settings.CLOVA_STUDIO_BASE_URL
        self.headers = {
            "Authorization": f"Bearer {self.api_key}",
            "Content-Type": "application/json"
        }
    
    def _validate_api_key(self):
        """API 키 유효성 검사"""
        if not self.api_key:
            raise HTTPException(
                status_code=500,
                detail="CLOVA Studio API 키가 설정되지 않았습니다"
            )
        print(f"🔑 API 키 확인: {self.api_key[:10]}...")  # 디버깅용
    
    def _prepare_headers(self, request_id: Optional[str] = None) -> Dict[str, str]:
        """요청 헤더 준비"""
        headers = self.headers.copy()
        if request_id:
            headers["X-NCP-CLOVASTUDIO-REQUEST-ID"] = request_id
        print(f"📋 요청 헤더: {headers}")  # 디버깅용
        return headers
    
    async def chat_completion(self, model_name: str, request_data: Dict[str, Any]) -> Dict[str, Any]:
        """일반 채팅 완성 API 호출"""
        self._validate_api_key()
        
        url = f"{self.base_url}/v3/chat-completions/{model_name}"
        headers = self._prepare_headers(request_data.get("requestId"))
        
        # 디버깅용 로깅
        print(f"🌐 요청 URL: {url}")
        print(f"📤 요청 데이터: {json.dumps(request_data, ensure_ascii=False, indent=2)}")
        
        try:
            response = requests.post(url, headers=headers, json=request_data)
            print(f"📥 응답 상태: {response.status_code}")
            print(f"📥 응답 헤더: {dict(response.headers)}")
            
            if response.status_code != 200:
                print(f"❌ 에러 응답: {response.text}")
            
            response.raise_for_status()
            
            response_data = response.json()
            # CLOVA Studio 응답을 그대로 반환 (이미 올바른 형식)
            return response_data
            
        except requests.exceptions.RequestException as e:
            print(f"🚫 API 호출 오류: {str(e)}")
            raise HTTPException(
                status_code=500,
                detail=f"CLOVA Studio API 호출 중 오류가 발생했습니다: {str(e)}"
            )
        except Exception as e:
            print(f"🚫 응답 처리 오류: {str(e)}")
            raise HTTPException(
                status_code=500,
                detail=f"응답 처리 중 오류가 발생했습니다: {str(e)}"
            )
    
    async def streaming_chat_completion(
        self, 
        model_name: str, 
        request_data: Dict[str, Any]
    ) -> AsyncGenerator[str, None]:
        """스트리밍 채팅 완성 API 호출"""
        self._validate_api_key()
        
        url = f"{self.base_url}/v3/chat-completions/{model_name}"
        headers = self._prepare_headers(request_data.get("requestId"))
        headers["Accept"] = "text/event-stream"
        
        try:
            response = requests.post(url, headers=headers, json=request_data, stream=True)
            response.raise_for_status()
            
            print(f"🔍 스트리밍 응답 시작: {response.status_code}")
            
            for line in response.iter_lines():
                if line:
                    line_str = line.decode('utf-8')
                    print(f"📥 스트리밍 라인: {line_str}")
                    
                    # SSE 형식 그대로 전달
                    if line_str.startswith('id: ') or line_str.startswith('event: ') or line_str.startswith('data: '):
                        yield f"{line_str}\n"
                    elif line_str.strip() == '':
                        yield "\n"
                    else:
                        # 기타 라인도 전달 (디버깅용)
                        yield f" {line_str}\n"
                                
        except requests.exceptions.RequestException as e:
            raise HTTPException(
                status_code=500,
                detail=f"CLOVA Studio API 스트리밍 호출 중 오류가 발생했습니다: {str(e)}"
            )
        except Exception as e:
            raise HTTPException(
                status_code=500,
                detail=f"스트리밍 응답 처리 중 오류가 발생했습니다: {str(e)}"
            )

# 서비스 인스턴스 생성
clova_service = ClovaService()
