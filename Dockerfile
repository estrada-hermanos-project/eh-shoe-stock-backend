# Imagen del microservicio eh-shoe-stock-backend (Java 21 / Spring Boot 3.5).
# MySQL y el volumen de datos NO van en esta imagen: un contenedor = un proceso.
# Se orquestan junto a esta imagen en docker-compose.yml.

# ---------- etapa de construcción ----------
FROM maven:3.9.9-eclipse-temurin-21-jammy AS build
WORKDIR /build

COPY pom.xml .
COPY src ./src

RUN mvn -B -DskipTests package \
    && mv target/eh-shoe-stock-backend-*.jar /build/app.jar

# ---------- etapa de ejecución ----------
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system spring \
    && useradd --system --gid spring --uid 1001 spring

COPY --from=build --chown=spring:spring /build/app.jar /app/app.jar

USER spring
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=50s --retries=5 \
    CMD curl -fsS http://127.0.0.1:8080/api/v1/health || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
