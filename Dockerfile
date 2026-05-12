# =============================================
# Spring Boot Dockerfile
# 빌드: GitHub Actions에서 처리
# 이 파일: 실행 이미지만 담당
# =============================================

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 보안: root 대신 전용 유저 실행
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# GitHub Actions가 빌드한 jar 복사
COPY build/libs/*.jar app.jar

RUN mkdir -p /app/logs

EXPOSE 8080

ENTRYPOINT ["java", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-Dspring.profiles.active=prod", \
  "-jar", "app.jar"]