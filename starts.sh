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
    echo "  --status        서비스 상태 확인"
    echo "  --logs          서비스 로그 확인"
    echo "  --troubleshoot  문제 해결 도구"
    echo ""
    echo -e "${YELLOW}예시:${NC}"
    echo "  ./starts.sh -d           # 개발 모드 실행"
    echo "  ./starts.sh -p -b        # 프로덕션 모드 강제 재빌드"
    echo "  ./starts.sh -c           # 전체 정리"
    echo "  ./starts.sh --fix-ports  # 포트 충돌 해결"
    echo "  ./starts.sh --troubleshoot # 문제 해결"
}

# 함수: 포트 충돌 해결
fix_port_conflicts() {
    echo -e "${YELLOW}🔧 포트 충돌 해결 중...${NC}"
    
    # 충돌하는 포트들 정의
    PORTS=(3000 8000 8080 8081 5432 6379)
    
    echo -e "${BLUE}충돌하는 컨테이너 확인 중...${NC}"
    
    # 먼저 모든 관련 컨테이너 강제 중지
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
    
    # 모든 실행 중인 컨테이너에서 포트 사용 확인 및 중지
    for port in "${PORTS[@]}"; do
        echo -e "${CYAN}포트 $port 확인 중...${NC}"
        
        # Docker 컨테이너에서 포트 사용 확인 (더 정확한 방법)
        container_ids=$(docker ps --format "{{.ID}}" --filter "publish=$port" 2>/dev/null || true)
        
        if [ ! -z "$container_ids" ]; then
            echo -e "${YELLOW}포트 $port를 사용하는 컨테이너 발견:${NC}"
            echo "$container_ids" | while read -r container_id; do
                if [ ! -z "$container_id" ]; then
                    container_name=$(docker inspect --format '{{.Name}}' "$container_id" 2>/dev/null | sed 's/^\/*//' || echo "unknown")
                    echo -e "${RED}  - $container_name ($container_id) 중지 중...${NC}"
                    docker stop "$container_id" || true
                    docker rm "$container_id" || true
                fi
            done
        fi
        
        # 추가로 포트 매핑 패턴으로 검색
        container_ids2=$(docker ps --format "table {{.ID}}\t{{.Ports}}" | grep ":$port->" | awk '{print $1}' | grep -v "CONTAINER" || true)
        
        if [ ! -z "$container_ids2" ]; then
            echo -e "${YELLOW}포트 $port를 사용하는 추가 컨테이너 발견:${NC}"
            echo "$container_ids2" | while read -r container_id; do
                if [ ! -z "$container_id" ]; then
                    container_name=$(docker inspect --format '{{.Name}}' "$container_id" 2>/dev/null | sed 's/^\/*//' || echo "unknown")
                    echo -e "${RED}  - $container_name ($container_id) 중지 중...${NC}"
                    docker stop "$container_id" || true
                    docker rm "$container_id" || true
                fi
            done
        fi
        
        # 시스템 포트 사용 확인 (Windows/WSL 호환)
        if command -v netstat &> /dev/null; then
            if netstat -an 2>/dev/null | grep -q ":$port "; then
                echo -e "${YELLOW}  포트 $port가 시스템에서 사용 중일 수 있습니다${NC}"
            fi
        fi
        
        echo -e "${GREEN}  포트 $port: 정리 완료${NC}"
    done
    
    # 네트워크 정리
    echo -e "${BLUE}Docker 네트워크 정리 중...${NC}"
    docker network prune -f 2>/dev/null || true
    
    # 잠시 대기하여 포트가 완전히 해제되도록 함
    echo -e "${CYAN}포트 해제 대기 중...${NC}"
    sleep 3
    
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
    
    # .env 파일 로드 및 export
    set -a  # 자동으로 모든 변수를 export
    source .env
    set +a  # 자동 export 비활성화
    
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
        docker-compose down --volumes --remove-orphans --timeout 10
        docker-compose build --no-cache
    fi
    
    # 서비스 시작 (개별 서비스 순차 시작으로 안정성 확보)
    echo -e "${CYAN}서비스 단계별 시작 중...${NC}"
    
    # 1단계: 데이터베이스 서비스 먼저 시작
    echo -e "${BLUE}  1️⃣ 데이터베이스 서비스 시작...${NC}"
    docker-compose --env-file .env up -d postgres redis
    sleep 5
    
    # 2단계: AI 서비스 시작
    echo -e "${BLUE}  2️⃣ AI 서비스 시작...${NC}"
    docker-compose --env-file .env up -d ai-service
    sleep 3
    
    # 3단계: 백엔드 서비스 시작
    echo -e "${BLUE}  3️⃣ 백엔드 서비스 시작...${NC}"
    docker-compose --env-file .env up -d backend
    sleep 3
    
    # 4단계: 프론트엔드 및 기타 서비스 시작
    echo -e "${BLUE}  4️⃣ 프론트엔드 및 관리 서비스 시작...${NC}"
    docker-compose --env-file .env up -d
    
    # 서비스 시작 확인
    echo -e "${CYAN}서비스 시작 확인 중...${NC}"
    sleep 5
    
    # 실패한 서비스가 있는지 확인
    failed_services=$(docker-compose ps --services --filter "status=exited")
    if [ ! -z "$failed_services" ]; then
        echo -e "${RED}❌ 일부 서비스 시작 실패:${NC}"
        echo "$failed_services"
        echo -e "${YELLOW}💡 로그 확인: docker-compose logs [서비스명]${NC}"
        echo -e "${YELLOW}💡 재시도: ./starts.sh -r${NC}"
        return 1
    fi
    
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

# 함수: 트러블슈팅
troubleshoot() {
    echo -e "${PURPLE}🔧 ChatGPT 클론 서비스 문제 해결 도구${NC}"
    echo ""
    
    echo -e "${CYAN}1. 현재 Docker 상태 확인${NC}"
    echo "Docker 버전:"
    docker --version
    echo "Docker Compose 버전:"
    docker-compose --version
    echo ""
    
    echo -e "${CYAN}2. 실행 중인 컨테이너${NC}"
    docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}\t{{.Image}}"
    echo ""
    
    echo -e "${CYAN}3. 중지된 컨테이너${NC}"
    docker ps -a --filter "status=exited" --format "table {{.Names}}\t{{.Status}}\t{{.Image}}"
    echo ""
    
    echo -e "${CYAN}4. 포트 사용 상황${NC}"
    PORTS=(3000 8000 8080 8081 5432 6379)
    for port in "${PORTS[@]}"; do
        echo -n "포트 $port: "
        if command -v netstat &> /dev/null; then
            if netstat -an 2>/dev/null | grep -q ":$port "; then
                echo -e "${RED}사용 중${NC}"
            else
                echo -e "${GREEN}사용 가능${NC}"
            fi
        else
            echo -e "${YELLOW}확인 불가${NC}"
        fi
    done
    echo ""
    
    echo -e "${CYAN}5. Docker 리소스 사용량${NC}"
    docker system df
    echo ""
    
    echo -e "${CYAN}6. 최근 컨테이너 로그 (마지막 20줄)${NC}"
    containers=$(docker ps --format "{{.Names}}" | grep -E "(chatgpt|gpt)" | head -3)
    if [ ! -z "$containers" ]; then
        for container in $containers; do
            echo -e "${YELLOW}=== $container 로그 ====${NC}"
            docker logs --tail 20 "$container" 2>&1 || echo "로그를 가져올 수 없습니다."
            echo ""
        done
    else
        echo "실행 중인 ChatGPT 관련 컨테이너가 없습니다."
    fi
    
    echo -e "${CYAN}7. 환경 변수 확인${NC}"
    if [ -f ".env" ]; then
        echo "✅ .env 파일 존재"
        if grep -q "OPENAI_API_KEY" .env; then
            if [ ! -z "$(grep 'OPENAI_API_KEY=' .env | cut -d'=' -f2)" ]; then
                echo "✅ OPENAI_API_KEY 설정됨"
            else
                echo -e "${RED}❌ OPENAI_API_KEY가 비어있음${NC}"
            fi
        else
            echo -e "${RED}❌ OPENAI_API_KEY가 설정되지 않음${NC}"
        fi
    else
        echo -e "${RED}❌ .env 파일이 없음${NC}"
    fi
    echo ""
    
    echo -e "${CYAN}8. Docker Compose 설정 확인${NC}"
    if [ -f "docker-compose.yml" ]; then
        echo "✅ docker-compose.yml 존재"
        echo "서비스 목록:"
        docker-compose config --services 2>/dev/null || echo "설정 파일에 오류가 있을 수 있습니다."
    else
        echo -e "${RED}❌ docker-compose.yml 파일이 없음${NC}"
    fi
    echo ""
    
    echo -e "${PURPLE}🛠️ 추천 해결 방법:${NC}"
    echo "1. 포트 충돌 해결: ./starts.sh --fix-ports"
    echo "2. 전체 정리 후 재시작: ./starts.sh -c && ./starts.sh"
    echo "3. 강제 재빌드: ./starts.sh -b"
    echo "4. 서비스 로그 확인: docker-compose logs -f [서비스명]"
    echo "5. 개별 컨테이너 재시작: docker restart [컨테이너명]"
}

# 함수: 서비스 로그 확인
show_logs() {
    echo -e "${BLUE}📋 서비스 로그 확인${NC}"
    echo ""
    
    # 사용자에게 어떤 서비스의 로그를 볼지 선택하게 함
    echo -e "${CYAN}사용 가능한 서비스:${NC}"
    services=$(docker-compose config --services 2>/dev/null)
    if [ ! -z "$services" ]; then
        echo "$services"
        echo ""
        echo -e "${YELLOW}전체 로그를 보려면 'all'을 입력하세요.${NC}"
        echo -e "${YELLOW}특정 서비스 로그를 보려면 서비스명을 입력하세요.${NC}"
        read -p "서비스명 (기본값: all): " service_name
        
        if [ -z "$service_name" ] || [ "$service_name" = "all" ]; then
            docker-compose logs -f --tail=50
        else
            docker-compose logs -f --tail=50 "$service_name"
        fi
    else
        echo -e "${RED}Docker Compose 서비스를 찾을 수 없습니다.${NC}"
        echo "실행 중인 컨테이너 로그:"
        docker logs $(docker ps --format "{{.Names}}" | head -1) --tail=50 -f 2>/dev/null || echo "로그를 가져올 수 없습니다."
    fi
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
        --troubleshoot)
            troubleshoot
            exit 0
            ;;
        --logs)
            show_logs
            exit 0
            ;;
        *)
            echo -e "${RED}❌ 알 수 없는 옵션: $1${NC}"
            show_help
            exit 1
            ;;
    esac
done

# 함수: 강력한 포트 충돌 해결
force_clean_all() {
    echo -e "${RED}🔥 강력한 정리 모드 실행 중...${NC}"
    
    # 모든 관련 컨테이너 강제 중지 및 삭제
    echo -e "${YELLOW}모든 ChatGPT 관련 컨테이너 중지 중...${NC}"
    docker ps -a --format '{{.Names}}' | grep -E "(chatgpt|gpt)" | xargs -r docker stop 2>/dev/null || true
    docker ps -a --format '{{.Names}}' | grep -E "(chatgpt|gpt)" | xargs -r docker rm -f 2>/dev/null || true
    
    # 추가 컨테이너들 정리
    containers_to_remove=(
        "redis" "postgres" "postgresql" "ai-service" "frontend" "backend"
        "redis-commander" "pgadmin"
    )
    
    for pattern in "${containers_to_remove[@]}"; do
        docker ps -a --format '{{.Names}}' | grep -i "$pattern" | head -5 | xargs -r docker stop 2>/dev/null || true
        docker ps -a --format '{{.Names}}' | grep -i "$pattern" | head -5 | xargs -r docker rm -f 2>/dev/null || true
    done
    
    # Docker Compose 서비스 강제 중지
    docker-compose down --volumes --remove-orphans --timeout 10 2>/dev/null || true
    docker-compose -f docker-compose.dev.yml down --volumes --remove-orphans --timeout 10 2>/dev/null || true
    
    # 네트워크 정리
    docker network ls --format '{{.Name}}' | grep -E "(chatgpt|gpt)" | xargs -r docker network rm 2>/dev/null || true
    docker network prune -f 2>/dev/null || true
    
    # 볼륨 정리
    docker volume ls --format '{{.Name}}' | grep -E "(chatgpt|gpt)" | xargs -r docker volume rm 2>/dev/null || true
    
    echo -e "${GREEN}✅ 강력한 정리 완료${NC}"
    sleep 2
}

# 메인 실행 로직
main() {
    echo -e "${BLUE}🚀 ChatGPT 클론 서비스 시작...${NC}"
    
    # 사전 검사
    check_docker
    check_env
    
    # 강력한 정리 및 포트 충돌 해결
    force_clean_all
    fix_port_conflicts
    
    # 추가 대기 시간
    echo -e "${CYAN}시스템 안정화 대기 중...${NC}"
    sleep 3
    
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
    sleep 8
    check_status
    
    echo ""
    echo -e "${GREEN}🎉 서비스가 성공적으로 시작되었습니다!${NC}"
    echo -e "${CYAN}💡 로그 확인: docker-compose logs -f${NC}"
    echo -e "${CYAN}💡 서비스 중지: ./starts.sh -s${NC}"
}

# 스크립트 실행
main