FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Copiamos el parent POM primero
COPY pom.xml /app/parent-pom.xml

# Copiamos el pom.xml del módulo
COPY user-service/pom.xml .

# Descargamos dependencias (cacheado si no cambia el pom.xml)
RUN mvn dependency:go-offline \
    -f pom.xml \
    -s /usr/share/maven/ref/settings-docker.xml \
    --no-transfer-progress 2>/dev/null || true

# Copiamos el código fuente
COPY user-service/src ./src

# Build — saltamos tests porque los corremos en CI
RUN mvn package -DskipTests --no-transfer-progress

# ── Imagen de producción ────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Usuario no-root
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]