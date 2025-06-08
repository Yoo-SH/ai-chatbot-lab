#!/bin/bash

# 🤖 ChatGPT 클론 서비스 Docker 빌드 & 실행 스크립트
# 작성자: ChatGPT Clone Team
# 설명: 프로덕션 및 개발 환경에서 Docker 컨테이너를 빌드하고 실행합니다.

set -e  # 오류 발생 시 스크립트 중단

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# 로고 출력
echo -e "${CYAN}"
echo "╔══════════════════════════════════════════════════╗"
echo "║              🤖 ChatGPT Clone Service           ║"
echo "║            Docker Build & Start Script          ║"
echo "╚══════════════════════════════════════════════════╝"
echo -e "${NC}"

# 함수: 도움말 출력
show_help() {
    echo -e "${YELLOW}사용법:${NC}"
    echo "  ./starts.sh [옵션]"
    echo ""
    echo -e "${YELLOW}옵션:${NC}"
    echo "  -h, --help      이 도움말 표시"
    echo "  -d, --dev       개발 모드로 실행"
    echo "  -p, --prod      프로덕션 모드로 실행 (기본값)"
    echo "  -b, --build     이미지 강제 재빌드"
    echo "  -c, --clean     모든 컨테이너와 볼륨 정리"
    echo "  -s, --stop      모든 서비스 중지"
    echo "  -r, --restart   서비스 재시작"
    echo "  --fix-ports     포트 충돌 해결"
    echo ""
    echo -e "${YELLOW}예시:${NC}"
    echo "  ./starts.sh -d           # 개발 모드 실행"
    echo "  ./starts.sh -p -b        # 프로덕션 모드 강제 재빌드"
    echo "  ./starts.sh -c           # 전체 정리"
    echo "  ./starts.sh --fix-ports  # 포트 충돌 해결"
}

# 함수: 포트 충돌 해결
fix_port_conflicts() {
    echo -e "${YELLOW}🔧 포트 충돌 해결 중...${NC}"
    
    # 충돌하는 포트들 정의
    PORTS=(3000 8000 8080 8081 5432 6379)
    
    echo -e "${BLUE}충돌하는 컨테이너 확인 중...${NC}"
    
    # 각 포트를 사용하는 컨테이너 찾기 및 중지
    for port in "${PORTS[@]}"; do
        echo -e "${CYAN}포트 $port 확인 중...${NC}"
        
        # 해당 포트를 사용하는 컨테이너 ID 찾기
        container_ids=$(docker ps --format "table {{.ID}}\t{{.Ports}}" | grep ":$port->" | awk '{print $1}' | grep -v "CONTAINER" || true)
        
        if [ ! -z "$container_ids" ]; then
            echo -e "${YELLOW}포트 $port를 사용하는 컨테이너 발견:${NC}"
            echo "$container_ids" | while read -r container_id; do
                if [ ! -z "$container_id" ]; then
                    container_name=$(docker inspect --format '{{.Name}}' "$container_id" | sed 's/^\/*//')
                    echo -e "${RED}  - $container_name ($container_id) 중지 중...${NC}"
                    docker stop "$container_id" || true
                    docker rm "$container_id" || true
                fi
            done
        else
            echo -e "${GREEN}  포트 $port: 사용 가능${NC}"
        fi
    done
    
    # 특정 이름의 컨테이너들 중지 (기존 GPT 관련 컨테이너들)
    echo -e "${BLUE}기존 GPT 관련 컨테이너 정리 중...${NC}"
    conflicting_containers=(
        "gpt-redis-only"
        "gpt-redis-commander"
        "chatgpt-redis"
        "chatgpt-postgres"
        "chatgpt-ai-service"
        "chatgpt-frontend"
        "chatgpt-backend"
    )
    
    for container in "${conflicting_containers[@]}"; do
        if docker ps -a --format '{{.Names}}' | grep -q "^${container}$"; then
            echo -e "${YELLOW}컨테이너 $container 중지 및 삭제 중...${NC}"
            docker stop "$container" 2>/dev/null || true
            docker rm "$container" 2>/dev/null || true
        fi
    done
    
    echo -e "${GREEN}✅ 포트 충돌 해결 완료!${NC}"
}

# 함수: 환경 변수 확인
check_env() {
    echo -e "${BLUE}🔍 환경 변수 확인 중...${NC}"
    
    if [ ! -f ".env" ]; then
        echo -e "${YELLOW}⚠️  .env 파일이 없습니다. env.example을 복사합니다...${NC}"
        if [ -f "env.example" ]; then
            cp env.example .env
            echo -e "${GREEN}✅ .env 파일이 생성되었습니다.${NC}"
            echo -e "${RED}❗ .env 파일에서 OPENAI_API_KEY를 설정해주세요!${NC}"
            echo ""
            read -p "계속하시겠습니까? (y/N): " -n 1 -r
            echo
            if [[ ! $REPLY =~ ^[Yy]$ ]]; then
                exit 1
            fi
        else
            echo -e "${RED}❌ env.example 파일을 찾을 수 없습니다!${NC}"
            exit 1
        fi
    fi
    
    # .env 파일 로드
    if [ -f ".env" ]; then
        export $(grep -v '^#' .env | xargs)
    fi
    
    # OPENAI_API_KEY 확인
    if [ -z "$OPENAI_API_KEY" ]; then
        echo -e "${RED}❌ OPENAI_API_KEY가 설정되지 않았습니다!${NC}"
        echo -e "${YELLOW}💡 .env 파일에서 OPENAI_API_KEY를 설정해주세요.${NC}"
        exit 1
    fi
    
    echo -e "${GREEN}✅ 환경 변수 확인 완료${NC}"
}

# 함수: Docker 및 Docker Compose 확인
check_docker() {
    echo -e "${BLUE}🐳 Docker 환경 확인 중...${NC}"
    
    if ! command -v docker &> /dev/null; then
        echo -e "${RED}❌ Docker가 설치되지 않았습니다!${NC}"
        exit 1
    fi
    
    if ! command -v docker-compose &> /dev/null; then
        echo -e "${RED}❌ Docker Compose가 설치되지 않았습니다!${NC}"
        exit 1
    fi
    
    # Docker 데몬 실행 확인
    if ! docker info &> /dev/null; then
        echo -e "${RED}❌ Docker 데몬이 실행되지 않았습니다!${NC}"
        echo -e "${YELLOW}💡 Docker Desktop을 시작하거나 Docker 서비스를 실행해주세요.${NC}"
        exit 1
    fi
    
    echo -e "${GREEN}✅ Docker 환경 확인 완료${NC}"
}

# 함수: 프로덕션 모드 실행
run_production() {
    echo -e "${PURPLE}🚀 프로덕션 모드로 실행 중...${NC}"
    
    if [ "$BUILD" = true ]; then
        echo -e "${YELLOW}🔨 이미지 재빌드 중...${NC}"
        docker-compose down --volumes --remove-orphans
        docker-compose build --no-cache
    fi
    
    # 서비스 시작
    docker-compose up -d
    
    echo -e "${GREEN}✅ 프로덕션 서비스 시작 완료!${NC}"
    echo ""
    echo -e "${CYAN}📊 서비스 접속 정보:${NC}"
    echo "  🌐 프론트엔드:    http://localhost:3000"
    echo "  🔧 백엔드 API:    http://localhost:8080"
    echo "  🤖 AI 서비스:     http://localhost:8000"
    echo "  🗄️  Redis 관리:   http://localhost:8081"
    echo "  📊 DB 관리:       localhost:5432 (postgres/3482)"
}

# 함수: 개발 모드 실행
run_development() {
    echo -e "${PURPLE}🛠️  개발 모드로 실행 중...${NC}"
    
    if [ "$BUILD" = true ]; then
        echo -e "${YELLOW}🔨 개발 이미지 재빌드 중...${NC}"
        docker-compose -f docker-compose.dev.yml down --volumes --remove-orphans
        docker-compose -f docker-compose.dev.yml build --no-cache
    fi
    
    # 개발 서비스 시작 (AI 서비스만)
    docker-compose -f docker-compose.dev.yml up -d
    
    echo -e "${GREEN}✅ 개발 환경 서비스 시작 완료!${NC}"
    echo ""
    echo -e "${CYAN}📊 개발 서비스 접속 정보:${NC}"
    echo "  🤖 AI 서비스:     http://localhost:8000"
    echo "  🗄️  Redis 관리:   http://localhost:8081"
    echo "  📊 DB 관리:       localhost:5432 (postgres/3482)"
    echo ""
    echo -e "${YELLOW}💡 프론트엔드와 백엔드는 로컬에서 실행하세요:${NC}"
    echo "  📁 백엔드: cd back/gpt && ./gradlew bootRun"
    echo "  📁 프론트엔드: cd front && npm start"
}

# 함수: 서비스 중지
stop_services() {
    echo -e "${YELLOW}🛑 모든 서비스 중지 중...${NC}"
    
    docker-compose down --remove-orphans
    docker-compose -f docker-compose.dev.yml down --remove-orphans
    
    echo -e "${GREEN}✅ 모든 서비스가 중지되었습니다.${NC}"
}

# 함수: 전체 정리
clean_all() {
    echo -e "${RED}🧹 전체 정리 중...${NC}"
    echo -e "${YELLOW}⚠️  이 작업은 모든 컨테이너, 이미지, 볼륨을 삭제합니다!${NC}"
    
    read -p "정말 진행하시겠습니까? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "작업이 취소되었습니다."
        exit 0
    fi
    
    # 컨테이너 중지 및 삭제
    docker-compose down --volumes --remove-orphans
    docker-compose -f docker-compose.dev.yml down --volumes --remove-orphans
    
    # ChatGPT 관련 이미지 삭제
    docker images | grep -E "(chatgpt|gpt)" | awk '{print $3}' | xargs -r docker rmi -f
    
    # 사용하지 않는 리소스 정리
    docker system prune -af --volumes
    
    echo -e "${GREEN}✅ 전체 정리가 완료되었습니다.${NC}"
}

# 함수: 서비스 상태 확인
check_status() {
    echo -e "${BLUE}📊 서비스 상태 확인 중...${NC}"
    
    echo -e "${CYAN}현재 실행 중인 컨테이너:${NC}"
    docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
    
    echo ""
    echo -e "${CYAN}서비스 헬스체크:${NC}"
    
    # 각 서비스 확인
    services=(
        "http://localhost:3000|프론트엔드"
        "http://localhost:8080/actuator/health|백엔드"
        "http://localhost:8000/health|AI 서비스"
        "http://localhost:8081|Redis 관리"
    )
    
    for service in "${services[@]}"; do
        IFS='|' read -r url name <<< "$service"
        if curl -s "$url" > /dev/null 2>&1; then
            echo -e "  ✅ $name: ${GREEN}정상${NC}"
        else
            echo -e "  ❌ $name: ${RED}비정상${NC}"
        fi
    done
}

# 기본값 설정
MODE="prod"
BUILD=false

# 명령행 인수 처리
while [[ $# -gt 0 ]]; do
    case $1 in
        -h|--help)
            show_help
            exit 0
            ;;
        -d|--dev)
            MODE="dev"
            shift
            ;;
        -p|--prod)
            MODE="prod"
            shift
            ;;
        -b|--build)
            BUILD=true
            shift
            ;;
        -c|--clean)
            clean_all
            exit 0
            ;;
        -s|--stop)
            stop_services
            exit 0
            ;;
        -r|--restart)
            stop_services
            MODE="prod"
            BUILD=false
            shift
            ;;
        --status)
            check_status
            exit 0
            ;;
        --fix-ports)
            fix_port_conflicts
            exit 0
            ;;
        *)
            echo -e "${RED}❌ 알 수 없는 옵션: $1${NC}"
            show_help
            exit 1
            ;;
    esac
done

# 메인 실행 로직
main() {
    echo -e "${BLUE}🚀 ChatGPT 클론 서비스 시작...${NC}"
    
    # 포트 충돌 자동 해결
    fix_port_conflicts
    
    # 기존 서비스 중지
    docker-compose -f docker-compose.dev.yml down --remove-orphans 2>/dev/null || true
    docker-compose -f docker-compose.yml down --remove-orphans 2>/dev/null || true
    
    # 사전 검사
    check_docker
    check_env
    
    # 모드에 따른 실행
    case $MODE in
        "dev")
            run_development
            ;;
        "prod")
            run_production
            ;;
    esac
    
    # 실행 후 상태 확인
    sleep 5
    check_status
    
    echo ""
    echo -e "${GREEN}🎉 서비스가 성공적으로 시작되었습니다!${NC}"
    echo -e "${CYAN}💡 로그 확인: docker-compose logs -f${NC}"
    echo -e "${CYAN}💡 서비스 중지: ./starts.sh -s${NC}"
}

# 스크립트 실행
main