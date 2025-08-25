from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from core.config import settings
from apis.v1.chat_completions import router as chat_completions_router
from apis.v1.tasks import router as tasks_router
from apis.v1.models import router as models_router

app = FastAPI(
    title=settings.PROJECT_NAME,
    version=settings.VERSION,
    docs_url="/docs",
    redoc_url="/redoc",
    openapi_url="/openapi.json",
    openapi_tags=[
        {
            "name": "chat-completions",
            "description": "CLOVA Studio Chat Completions API"
        },
        {
            "name": "tasks",
            "description": "CLOVA Studio Task-based Chat Completions API"
        },
        {
            "name": "models",
            "description": "Available models information"
        }
    ]
)

# CORS 미들웨어 설정
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# API 라우터 등록
app.include_router(chat_completions_router, prefix=settings.API_V1_STR)
app.include_router(tasks_router, prefix=settings.API_V1_STR)
app.include_router(models_router, prefix=settings.API_V1_STR)

@app.get("/")
async def root():
    return {
        "message": "CLOVAX API",
        "version": settings.VERSION,
        "docs": "/docs"
    }

@app.get("/health")
async def health_check():
    return {"status": "healthy"}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        "main:app",
        host=settings.HOST,
        port=settings.PORT,
        reload=settings.RELOAD,
        debug=settings.DEBUG
    )
