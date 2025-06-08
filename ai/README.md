# OpenAI GPT API 예제 코드

OpenAI GPT API를 활용한 파이썬 채팅 서비스 예제입니다. 모듈별로 기능이 분리되어 있어 확장성과 유지보수성이 뛰어납니다.

## 📁 프로젝트 구조

```
ai/
├── __init__.py          # 패키지 초기화 및 편리한 import 제공
├── main.py              # 메인 실행 파일 (통합 메뉴)
├── openai_service.py    # OpenAI API 서비스 클래스
├── chatbot.py           # 대화형 챗봇 인터페이스
├── examples.py          # 다양한 사용 예제들
├── setup_venv.py        # 가상환경 자동 설정 스크립트
├── requirements.txt     # 필요한 패키지 목록
├── .gitignore           # Git 제외 파일 목록
└── README.md           # 이 파일
```

## 🚀 주요 기능

- ✨ **기본 채팅**: 일반적인 질문-답변 형태의 채팅
- 🔄 **스트리밍 응답**: 실시간으로 AI 응답을 받아보기
- 💾 **대화 기록 관리**: 대화 내용 저장/불러오기
- 🎯 **커스텀 프롬프트**: 시스템 프롬프트로 AI 성격 조정
- 🤖 **대화형 챗봇**: 터미널에서 직접 채팅 가능
- 🔧 **고급 설정**: 모델 변경, 창의성 조정 등
- 📚 **다양한 예제**: 8가지 실용적인 사용 예제
- 🏠 **가상환경**: 독립적인 Python 환경 지원

## 📦 설치 및 설정

### 🎯 빠른 시작 (자동 설정)

**1단계: 가상환경 자동 설정**
```bash
cd ai
python setup_venv.py
```

**2단계: 가상환경 활성화**
```bash
# Windows
venv\Scripts\activate

# Linux/Mac
source venv/bin/activate
```

**3단계: API 키 설정**
- `.env` 파일을 열어서 `your-api-key-here`를 실제 OpenAI API 키로 변경

**4단계: 프로그램 실행**
```bash
python main.py
```

### 🔧 수동 설정

#### 1. 가상환경 생성 및 활성화
```bash
cd ai

# 가상환경 생성
python -m venv venv

# 가상환경 활성화
# Windows:
venv\Scripts\activate

# Linux/Mac:
source venv/bin/activate
```

#### 2. 패키지 설치
```bash
pip install --upgrade pip
pip install -r requirements.txt
```

#### 3. OpenAI API 키 설정
다음 중 하나의 방법으로 API 키를 설정하세요:

##### 방법 1: .env 파일 (권장)
```bash
# .env 파일 생성
echo "OPENAI_API_KEY=your-api-key-here" > .env
```

##### 방법 2: 환경변수 설정
```bash
# Windows
set OPENAI_API_KEY=your-api-key-here

# Linux/Mac
export OPENAI_API_KEY="your-api-key-here"
```

##### 방법 3: 코드에서 직접 설정
```python
ai_service = OpenAIService(api_key="your-api-key-here")
```

#### 4. API 키 발급 방법
1. [OpenAI 플랫폼](https://platform.openai.com/api-keys)에 접속
2. 계정 로그인 또는 회원가입
3. API Keys 메뉴에서 새 API 키 생성
4. 생성된 키를 복사하여 설정

## 🎮 사용법

### 통합 프로그램 실행 (권장)
```bash
# 가상환경 활성화 후
python main.py
```

실행하면 다음과 같은 메뉴가 나타납니다:
```
🚀 OpenAI GPT API 통합 프로그램
==================================================

📋 사용 가능한 모드:
  1. 🤖 기본 챗봇 - 간단한 대화형 인터페이스
  2. 🚀 고급 챗봇 - 설정 변경 가능한 인터페이스
  3. 📚 예제 모음 - 다양한 사용 예제 실행
  4. ⚙️  환경 설정 확인
  5. ❓ 도움말
  6. 👋 종료
```

### 개별 모듈 사용

#### 1. OpenAI 서비스 직접 사용
```python
from openai_service import OpenAIService

# 서비스 초기화 (자동으로 .env 파일에서 API 키 로드)
ai_service = OpenAIService()

# 기본 채팅
response = ai_service.chat("안녕하세요! 파이썬에 대해 알려주세요.")
print(response)

# 스트리밍 채팅
for chunk in ai_service.chat_stream("파이썬의 장점을 설명해주세요."):
    print(chunk, end="", flush=True)
```

#### 2. 챗봇 인터페이스 사용
```python
from chatbot import ChatBot, AdvancedChatBot

# 기본 챗봇
basic_bot = ChatBot()
basic_bot.start_chat()

# 고급 챗봇 (모델/온도 설정 가능)
advanced_bot = AdvancedChatBot()
advanced_bot.start_chat()
```

#### 3. 예제 실행
```python
from examples import example_basic_chat, example_streaming_chat

# 기본 채팅 예제
example_basic_chat()

# 스트리밍 채팅 예제
example_streaming_chat()
```

#### 4. 패키지 레벨 import
```python
import ai

# 직접 클래스 사용
service = ai.OpenAIService()
chatbot = ai.ChatBot()

# 예제 함수 실행
ai.example_basic_chat()
```

## 🔧 모듈별 상세 기능

### 📡 OpenAIService (`openai_service.py`)
OpenAI API와의 모든 상호작용을 담당하는 핵심 클래스

**주요 메서드:**
- `chat()`: 일반 채팅 (전체 응답 한 번에)
- `chat_stream()`: 스트리밍 채팅 (실시간 응답)
- `generate_title()`: 대화 내용 기반 제목 생성
- `save_conversation()`: 대화 기록 저장
- `load_conversation()`: 대화 기록 불러오기
- `clear_conversation()`: 대화 기록 초기화
- `get_conversation_summary()`: 대화 통계

**환경변수 지원:**
- `OPENAI_API_KEY`: OpenAI API 키
- `DEFAULT_MODEL`: 기본 사용 모델 (기본값: gpt-3.5-turbo)
- `DEFAULT_TEMPERATURE`: 기본 창의성 수준 (기본값: 0.7)
- `DEFAULT_MAX_TOKENS`: 기본 최대 토큰 수 (기본값: 2000)

### 🤖 ChatBot (`chatbot.py`)
터미널 기반 대화형 인터페이스

**기본 챗봇 명령어:**
- `quit`, `exit`, `종료`: 채팅 종료
- `clear`, `초기화`: 대화 기록 초기화
- `history`: 대화 기록 확인
- `save <파일명>`: 대화 저장
- `load <파일명>`: 대화 불러오기
- `models`: 사용 가능한 모델 목록
- `stats`: 대화 통계
- `help`: 도움말

**고급 챗봇 추가 명령어:**
- `set-prompt <프롬프트>`: 시스템 프롬프트 설정
- `set-model <모델명>`: AI 모델 변경
- `set-temp <0.0-1.0>`: 창의성 수준 조정
- `show-settings`: 현재 설정 확인

### 📚 Examples (`examples.py`)
8가지 실용적인 사용 예제 제공

1. **기본 채팅**: 질문-답변 형태
2. **스트리밍 채팅**: 실시간 응답 확인
3. **커스텀 프롬프트**: AI 성격 조정
4. **다양한 모델**: GPT-3.5 vs GPT-4 비교
5. **창의성 수준 변화**: Temperature 값 변화 효과
6. **대화 기록 관리**: 저장/불러오기/통계
7. **제목 생성**: 자동 제목 생성
8. **코드 어시스턴트**: 프로그래밍 도움

### 🎯 Main (`main.py`)
통합 메뉴 시스템으로 모든 기능에 쉽게 접근

- 환경 설정 자동 확인
- API 연결 테스트
- 단계별 도움말 제공
- 오류 처리 및 복구

### 🏠 Setup (`setup_venv.py`)
가상환경 자동 설정 도구

- Python 버전 확인
- 가상환경 생성
- 패키지 자동 설치
- .env 파일 템플릿 생성
- 사용법 안내

## ⚙️ 고급 설정

### .env 파일 설정
```env
# OpenAI API 설정
OPENAI_API_KEY=sk-your-actual-api-key-here

# 기본 설정 (선택사항)
DEFAULT_MODEL=gpt-3.5-turbo
DEFAULT_TEMPERATURE=0.7
DEFAULT_MAX_TOKENS=2000
```

### 모델 선택
```python
# GPT-4 사용 (API 권한 필요)
response = ai_service.chat("질문", model="gpt-4")

# GPT-3.5 사용 (기본값)
response = ai_service.chat("질문", model="gpt-3.5-turbo")
```

### 창의성 조정
```python
# 보수적 응답 (정확성 중심)
response = ai_service.chat("질문", temperature=0.2)

# 창의적 응답 (다양성 중심)
response = ai_service.chat("질문", temperature=0.9)
```

### 시스템 프롬프트
```python
system_prompt = """
당신은 친근한 프로그래밍 튜터입니다.
초보자도 이해하기 쉽게 설명해주세요.
"""

response = ai_service.chat("파이썬이란?", system_prompt=system_prompt)
```

## 🔍 환경 설정 확인

프로그램 실행 시 자동으로 다음 항목들을 확인합니다:

- ✅ OpenAI API 키 설정 여부
- ✅ 필요한 패키지 설치 상태
- ✅ API 연결 테스트
- ✅ Python 버전 호환성
- ✅ 가상환경 활성화 상태

## 🚨 문제 해결

### 자주 발생하는 오류

#### 1. API 키 오류
```
ValueError: OpenAI API 키가 필요합니다.
```
**해결**: 
- `.env` 파일에서 API 키 확인
- 환경변수 `OPENAI_API_KEY` 설정 확인
- OpenAI 플랫폼에서 API 키 재발급

#### 2. 모듈 import 오류  
```
ModuleNotFoundError: No module named 'openai'
```
**해결**: 
- 가상환경이 활성화되었는지 확인
- `pip install -r requirements.txt` 실행

#### 3. 가상환경 활성화 오류
```
'venv' is not recognized as an internal or external command
```
**해결**: 
- `python setup_venv.py` 실행하여 가상환경 재설정
- 수동으로 `python -m venv venv` 실행

#### 4. API 호출 오류
```
openai.error.RateLimitError: Rate limit exceeded
```
**해결**: 잠시 대기 후 재시도 또는 API 사용량 확인

#### 5. 모델 접근 권한 오류
```
GPT-4 사용 불가
```
**해결**: OpenAI 계정에서 GPT-4 API 접근 권한 확인

## 💡 개발 팁

### 가상환경 관리
```bash
# 가상환경 활성화 (매번 작업 시작 시)
# Windows: venv\Scripts\activate
# Linux/Mac: source venv/bin/activate

# 가상환경 비활성화 (작업 완료 후)
deactivate

# 패키지 목록 업데이트
pip freeze > requirements.txt
```

### 새로운 기능 추가
1. **새로운 예제 추가**: `examples.py`에 함수 추가
2. **챗봇 명령어 추가**: `chatbot.py`의 `_handle_command()` 수정
3. **API 기능 확장**: `openai_service.py`에 메서드 추가

### 커스터마이징
- 기본 모델 변경: `.env` 파일에서 `DEFAULT_MODEL` 설정
- 기본 온도 변경: `.env` 파일에서 `DEFAULT_TEMPERATURE` 설정
- 로깅 레벨 조정: `logging.basicConfig(level=logging.DEBUG)`

## 🛡️ 보안 주의사항

1. **API 키 보안**: 
   - `.env` 파일을 Git에 커밋하지 마세요
   - API 키를 코드에 직접 하드코딩하지 마세요
   - 환경변수나 .env 파일을 사용하세요

2. **비용 관리**: 
   - OpenAI API는 사용량에 따라 비용이 발생합니다
   - API 사용량을 정기적으로 모니터링하세요

3. **Rate Limiting**: 
   - API 호출 횟수 제한에 주의하세요
   - 과도한 요청 시 일시적으로 차단될 수 있습니다

## 📄 라이센스

이 예제 코드는 교육 목적으로 자유롭게 사용할 수 있습니다.

## 🤝 기여하기

버그 리포트나 기능 개선 제안은 언제든 환영합니다!

---

**🎯 시작 권장 순서:**
1. `python setup_venv.py` 실행 (자동 설정)
2. 가상환경 활성화
3. `.env` 파일에서 API 키 설정
4. `python main.py` 실행
5. 메뉴 4번으로 환경 설정 확인
6. 메뉴 3번에서 기본 예제 체험
7. 메뉴 1번으로 챗봇 사용 시작 