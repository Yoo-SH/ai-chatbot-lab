import httpx
import asyncio
from typing import List, Optional, Dict, Any
import logging
import os
import numpy as np
from langchain.embeddings.base import Embeddings

logger = logging.getLogger(__name__)

class ClovaStudioEmbeddingService(Embeddings):
    """Clova Studio BGE-M3 임베딩 서비스"""
    
    def __init__(self):
        self.api_key = os.getenv("CLOVA_STUDIO_API_KEY")
        self.request_id = os.getenv("CLOVA_STUDIO_REQUEST_ID")
        self.base_url = "https://clovastudio.stream.ntruss.com"
        self.endpoint = "/v1/api-tools/embedding/v2"
        self.model_name = "bge-m3"
        self.embedding_dimension = 1024  # BGE-M3의 임베딩 차원
        
        if not self.api_key:
            raise ValueError("CLOVA_STUDIO_API_KEY 환경변수가 설정되어 있지 않습니다.")
    
    async def _make_api_request(self, text: str) -> Dict[str, Any]:
        """
        Clova Studio 임베딩 API 호출
        
        Args:
            text: 입력 텍스트 (최대 8192자)
            
        Returns:
            Dict: API 응답
        """
        headers = {
            "Content-Type": "application/json",
            "Authorization": f"Bearer {self.api_key}",
        }
        
        if self.request_id:
            headers["X-NCP-CLOVASTUDIO-REQUEST-ID"] = self.request_id
        
        payload = {
            "text": text
        }
        
        async with httpx.AsyncClient(timeout=60.0) as client:
            response = await client.post(
                f"{self.base_url}{self.endpoint}",
                json=payload,
                headers=headers
            )
            response.raise_for_status()
            return response.json()
    
    def _truncate_text(self, text: str, max_tokens: int = 8000) -> str:
        """
        입력 텍스트를 최대 길이로 자르기
        (한글 기준 1자 = 1토큰으로 간주)
        
        Args:
            text: 입력 텍스트
            max_tokens: 최대 토큰 수
            
        Returns:
            str: 자른 텍스트
        """
        if len(text) > max_tokens:
            logger.warning(f"입력 텍스트가 {max_tokens}자를 초과합니다. 잘라서 사용합니다.")
            return text[:max_tokens]
        return text
    
    async def aembed_documents(self, texts: List[str]) -> List[List[float]]:
        """
        여러 문서 임베딩 (비동기)
        
        Args:
            texts: 입력 텍스트 리스트
            
        Returns:
            List[List[float]]: 임베딩 벡터 리스트
        """
        embeddings = []
        
        for i, text in enumerate(texts):
            try:
                # 텍스트 자르기
                truncated_text = self._truncate_text(text)
                
                response = await self._make_api_request(truncated_text)
                
                if response.get("status", {}).get("code") != "20000":
                    raise Exception(f"API 오류: {response.get('status', {}).get('message', 'Unknown error')}")
                
                result = response.get("result", {})
                embedding = result.get("embedding", [])
                
                if len(embedding) != self.embedding_dimension:
                    raise Exception(f"임베딩 차원 불일치 (응답: {len(embedding)} / 기대: {self.embedding_dimension})")
                
                embeddings.append(embedding)
                logger.debug(f"문서 {i+1}/{len(texts)} 임베딩 완료 (입력 토큰: {result.get('inputTokens', 'N/A')})")
                
                # API 호출 간 딜레이 (rate limit 대응)
                if i < len(texts) - 1:
                    await asyncio.sleep(0.1)
                    
            except Exception as e:
                logger.error(f"문서 {i+1} 임베딩 실패: {str(e)}")
                # 실패한 문서는 결과에서 제외됨
                pass
        
        logger.info(f"총 {len(embeddings)}개 문서 임베딩 완료 (요청: {len(texts)}개)")
        return embeddings
    
    async def aembed_query(self, text: str) -> List[float]:
        """
        쿼리 임베딩 (비동기)
        
        Args:
            text: 입력 쿼리 텍스트
            
        Returns:
            List[float]: 임베딩 벡터
        """
        try:
            truncated_text = self._truncate_text(text)
            response = await self._make_api_request(truncated_text)
            
            if response.get("status", {}).get("code") != "20000":
                raise Exception(f"API 오류: {response.get('status', {}).get('message', 'Unknown error')}")
            
            result = response.get("result", {})
            embedding = result.get("embedding", [])
            
            if len(embedding) != self.embedding_dimension:
                raise Exception(f"임베딩 차원 불일치 (응답: {len(embedding)} / 기대: {self.embedding_dimension})")
            
            logger.debug(f"쿼리 임베딩 완료 (입력 토큰: {result.get('inputTokens', 'N/A')})")
            return embedding
            
        except Exception as e:
            logger.error(f"쿼리 임베딩 실패: {str(e)}")
            return [0.0] * self.embedding_dimension
    
    def embed_documents(self, texts: List[str]) -> List[List[float]]:
        """
        여러 문서 임베딩 (동기)
        
        Args:
            texts: 입력 텍스트 리스트
            
        Returns:
            List[List[float]]: 임베딩 벡터 리스트
        """
        try:
            loop = asyncio.get_event_loop()
        except RuntimeError:
            loop = asyncio.new_event_loop()
            asyncio.set_event_loop(loop)
        
        return loop.run_until_complete(self.aembed_documents(texts))
    
    def embed_query(self, text: str) -> List[float]:
        """
        쿼리 임베딩 (동기)
        
        Args:
            text: 입력 쿼리 텍스트
            
        Returns:
            List[float]: 임베딩 벡터
        """
        try:
            loop = asyncio.get_event_loop()
        except RuntimeError:
            loop = asyncio.new_event_loop()
            asyncio.set_event_loop(loop)
        
        return loop.run_until_complete(self.aembed_query(text))
    
    def cosine_similarity(self, vec1: List[float], vec2: List[float]) -> float:
        """
        코사인 유사도 계산 (BGE-M3는 코사인 유사도 기반)
        
        Args:
            vec1: 첫 번째 벡터
            vec2: 두 번째 벡터
            
        Returns:
            float: 코사인 유사도 (0~1)
        """
        vec1_np = np.array(vec1)
        vec2_np = np.array(vec2)
        
        dot_product = np.dot(vec1_np, vec2_np)
        norm1 = np.linalg.norm(vec1_np)
        norm2 = np.linalg.norm(vec2_np)
        
        if norm1 == 0 or norm2 == 0:
            return 0.0
        
        similarity = dot_product / (norm1 * norm2)
        # -1~1 범위를 0~1로 변환
        return (similarity + 1) / 2
    
    def get_embedding_dimension(self) -> int:
        """
        임베딩 차원 반환
        
        Returns:
            int: 임베딩 차원
        """
        return self.embedding_dimension
    
    def get_model_name(self) -> str:
        """
        모델명 반환
        
        Returns:
            str: 모델명
        """
        return self.model_name

# 싱글톤 인스턴스
clova_embedding_service = ClovaStudioEmbeddingService()