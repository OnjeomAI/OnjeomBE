FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 로그 디렉토리 먼저 생성 (root 권한으로)
RUN mkdir -p /app/logs

# 보안: root 대신 전용 유저 실행
RUN addgroup -S spring && adduser -S spring -G spring

# 로그 디렉토리 소유권 변경
RUN chown -R spring:spring /app

USER spring:spring

COPY build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-Dspring.profiles.active=prod", \
  "-jar", "app.jar"]