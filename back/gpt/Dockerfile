# 멀티스테이지 빌드를 위한 첫 번째 스테이지 (개선된 버전)
FROM gradle:8-jdk17 AS build

# 네트워크 연결 안정성을 위한 환경 변수 설정
ENV GRADLE_OPTS="-Dorg.gradle.daemon=false -Dorg.gradle.parallel=true -Dorg.gradle.workers.max=4"
ENV GRADLE_USER_HOME=/gradle

# 작업 디렉토리 설정
WORKDIR /app

# Gradle wrapper 및 설정 파일들 복사 (캐시 최적화)
COPY gradle ./gradle
COPY gradlew ./
COPY gradlew.bat ./
COPY build.gradle ./
COPY settings.gradle ./

# Gradle wrapper 실행 권한 부여
RUN chmod +x ./gradlew

# 의존성 다운로드 (재시도 로직 포함)
RUN ./gradlew dependencies --no-daemon --stacktrace --info || \
    ./gradlew dependencies --no-daemon --stacktrace --info || \
    ./gradlew dependencies --no-daemon --stacktrace --info

# 소스 코드 복사
COPY src ./src

# 애플리케이션 빌드 (재시도 로직 포함)
RUN ./gradlew clean bootJar --no-daemon --stacktrace --info || \
    ./gradlew clean bootJar --no-daemon --stacktrace --info

# 두 번째 스테이지 - 실행 환경 (경량화)
FROM openjdk:17-jdk-slim

# 필요한 패키지 설치 (최소화)
RUN apt-get update && apt-get install -y \
    curl \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# 작업 디렉토리 설정
WORKDIR /app

# 첫 번째 스테이지에서 빌드된 JAR 파일 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 애플리케이션 실행을 위한 사용자 생성
RUN addgroup --system spring && adduser --system spring --ingroup spring
RUN chown spring:spring /app/app.jar
USER spring:spring

# 포트 노출
EXPOSE 8080

# 헬스체크 설정 (타임아웃 연장)
HEALTHCHECK --interval=30s --timeout=15s --start-period=90s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# JVM 옵션 최적화
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC -XX:G1HeapRegionSize=16m"

# 애플리케이션 실행
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar -Dspring.profiles.active=docker /app/app.jar"]