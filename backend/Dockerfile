# Multi-stage build for Micronaut Backend with Java 25
FROM eclipse-temurin:25-jdk-noble AS builder
WORKDIR /app

# Cache Gradle wrapper and dependencies
COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle settings.gradle gradle.properties micronaut-cli.yml ./
RUN chmod +x gradlew && ./gradlew --version

# Copy source code and build fat shadow JAR
COPY src src
RUN ./gradlew shadowJar --no-daemon -x test

# Runtime Stage: Ultra-light JRE 25 image
FROM eclipse-temurin:25-jre-noble
WORKDIR /app

# Security: Run as non-root user
RUN groupadd -r peermesh && useradd -r -g peermesh -m -d /app peermesh
USER peermesh

COPY --from=builder --chown=peermesh:peermesh /app/build/libs/peermesh-*-all.jar app.jar

EXPOSE 8085
ENV PORT=8085
ENV MICRONAUT_ENVIRONMENTS=postgres

ENTRYPOINT ["java", "-XX:+UseVirtualThreads", "-jar", "app.jar"]
