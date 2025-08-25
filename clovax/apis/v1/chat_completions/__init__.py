from .chat import router as chat_router
from .streaming import router as streaming_router

# 두 라우터를 하나로 결합
from fastapi import APIRouter

router = APIRouter()
router.include_router(chat_router, prefix="/chat-completions")
router.include_router(streaming_router, prefix="/chat-completions")
