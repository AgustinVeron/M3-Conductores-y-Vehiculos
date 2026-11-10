# syntax=docker/dockerfile:1

# Etapa 1: Build
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build

COPY pom.xml mvnw ./
COPY .mvn/ .mvn/

# Normaliza finales de línea de Windows (CRLF a LF) y otorga permisos de ejecución
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw

RUN ./mvnw dependency:go-offline -B || true

COPY src/ src/
RUN ./mvnw clean package -Dmaven.test.skip=true -B

# Etapa 2: Runtime
FROM eclipse-temurin:21-jre-alpine AS runner
WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=builder /build/target/*.jar app.jar
RUN chown -R appuser:appgroup /app

USER appuser

EXPOSE 3003

HEALTHCHECK --interval=30s --timeout=5s --start-period=25s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:3003/health/live || exit 1

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-jar", "app.jar"]
