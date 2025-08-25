"""
프롬프트 관리 모듈
- 다양한 AI 작업을 위한 프롬프트들을 중앙 관리
- 각 프롬프트별 최적화된 설정 포함
- 유지보수 편의성 향상
"""

# 대화 제목 생성 설정
CONVERSATION_TITLE_CONFIG = {
    "prompt": """다음 대화 내용을 바탕으로 간단하고 명확한 제목을 생성해주세요. 
요구사항:
- 10글자 이내로 작성
- 대화의 핵심 주제를 반영
- 한국어로 작성""",
    "temperature": 0.3,  # 창의성보다는 일관성 중시
    "max_tokens": 50     # 짧은 제목이므로 적은 토큰
}

# 일반 채팅 설정
GENERAL_CHAT_CONFIG = {
    "prompt": """당신은 도움이 되고 친근한 AI 어시스턴트입니다. 
사용자의 질문에 정확하고 유용한 답변을 제공해주세요.""",
    "temperature": 0.7,  # 균형잡힌 창의성
    "max_tokens": 2000   # 충분한 답변 길이
}

# 코드 어시스턴트 설정
CODE_ASSISTANT_CONFIG = {
    "prompt": """당신은 전문적인 프로그래밍 어시스턴트입니다.
코드 작성, 디버깅, 최적화에 도움을 드립니다.
명확하고 실행 가능한 코드 예제를 제공해주세요.""",
    "temperature": 0.2,  # 정확성 중시
    "max_tokens": 3000   # 긴 코드 설명을 위한 많은 토큰
}

# 일기 감정 분석 설정
DIARY_EMOTION_ANALYSIS_CONFIG = {
    "prompt": """You are an AI Diary Assistant specialized in emotion analysis. 
When the user sends a diary entry, you must:
1. Analyze the text and detect primary and secondary emotions.  
2. 구체적인 감정 강도(0~100)와 감정 카테고리(예: 기쁨, 슬픔, 분노, 불안, 평온 등)를 JSON으로 반환.  
3. 요약(summary): 2–3문장으로 오늘의 정서 요약.  
4. 코칭(coaching): 내일을 위한 제안 또는 격려의 말을 최소 100글자 이상 토큰을 전부 소진.  
5. 모든 출력은 valid JSON 객체 하나로만 응답.
6. 불안지수가 50이상이면 isNegative를 true로 반환

Example JSON schema:
```json
{
  "aiResponse":{
    "emotions": [
        {"category": "기쁨",    "intensity": 75},
        {"category": "불안",    "intensity": 40},
        {"category": "분노",    "intensity": 10}
    ],
    "summary": "오늘은 새로운 프로젝트에 도전하며 뿌듯함을 느꼈으나, 마감이 다가와 약간의 불안도 있었습니다.",
    "coaching": "작은 성취부터 차근차근 기록하며 불안을 줄여보세요! 때로는 모든 것을 버리고 새로 시작해보는 것도 좋지만 그렇게 하기 전에 먼저 오늘 하루를 돌아보고 내일을 위한 준비를 해보세요!"
  }
  "isNegative": true
  "timestamp": "2025-06-16T10:00:00Z"
}
```""",
    "temperature": 0.4,  # 감정 분석의 일관성과 약간의 창의성
    "max_tokens": 1500   # JSON 응답을 위한 충분한 토큰
}

# 하위 호환성을 위한 기존 변수들 (deprecated)
CONVERSATION_TITLE_PROMPT = CONVERSATION_TITLE_CONFIG["prompt"]
GENERAL_CHAT_PROMPT = GENERAL_CHAT_CONFIG["prompt"]
CODE_ASSISTANT_PROMPT = CODE_ASSISTANT_CONFIG["prompt"]
DIARY_EMOTION_ANALYSIS_PROMPT = DIARY_EMOTION_ANALYSIS_CONFIG["prompt"]

# 설정 가져오기 헬퍼 함수들
def get_config(config_name: str) -> dict:
    """설정을 가져오는 헬퍼 함수"""
    configs = {
        "conversation_title": CONVERSATION_TITLE_CONFIG,
        "general_chat": GENERAL_CHAT_CONFIG,
        "code_assistant": CODE_ASSISTANT_CONFIG,
        "diary_emotion": DIARY_EMOTION_ANALYSIS_CONFIG
    }
    return configs.get(config_name, GENERAL_CHAT_CONFIG)

def get_prompt(config_name: str) -> str:
    """프롬프트만 가져오는 함수"""
    return get_config(config_name)["prompt"]

def get_temperature(config_name: str) -> float:
    """온도 설정만 가져오는 함수"""
    return get_config(config_name)["temperature"]

def get_max_tokens(config_name: str) -> int:
    """최대 토큰 설정만 가져오는 함수"""
    return get_config(config_name)["max_tokens"]

# 사용 예제
if __name__ == "__main__":
    # 설정 전체 가져오기
    chat_config = get_config("general_chat")
    print("일반 채팅 설정:", chat_config)
    
    # 개별 설정 가져오기
    print("프롬프트:", get_prompt("code_assistant"))
    print("온도:", get_temperature("code_assistant"))
    print("최대 토큰:", get_max_tokens("code_assistant"))
    
    # 모든 설정 출력
    print("\n=== 모든 프롬프트 설정 ===")
    for name in ["conversation_title", "general_chat", "code_assistant", "diary_emotion"]:
        config = get_config(name)
        print(f"\n{name}:")
        print(f"  온도: {config['temperature']}")
        print(f"  토큰: {config['max_tokens']}")
        print(f"  프롬프트: {config['prompt'][:50]}...")
