from .models import router as models_router

# models 라우터에 prefix 추가
from fastapi import APIRouter

router = APIRouter()
router.include_router(models_router, prefix="/models")
