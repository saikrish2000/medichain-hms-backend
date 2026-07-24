# ── Stage 1: Build ────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -q

COPY src ./src
RUN mvn clean package -DskipTests -q

# ── Stage 2: Run ─────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

RUN addgroup -S medichain && adduser -S medichain -G medichain

COPY --from=builder /app/target/*.jar app.jar
RUN chown medichain:medichain app.jar

USER medichain

EXPOSE 8080

ENTRYPOINT ["java", \
  "-Xmx450m", \
  "-Xms128m", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
