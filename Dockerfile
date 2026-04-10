# ── Stage 1: Build ────────────────────────────────────────────
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app
COPY pom.xml .
COPY src ./src

# Download dependencies first (better layer caching)
RUN apk add --no-cache maven && \
    mvn dependency:go-offline -q && \
    mvn clean package -DskipTests -q

# ── Stage 2: Run ─────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Non-root user for security
RUN addgroup -S medichain && adduser -S medichain -G medichain

COPY --from=builder /app/target/*.jar app.jar
RUN chown medichain:medichain app.jar

USER medichain

EXPOSE 8080

ENTRYPOINT ["java", \
  "-Xmx512m", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
