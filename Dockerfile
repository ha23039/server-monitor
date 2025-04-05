FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY . .
RUN chmod +x mvnw
# Aseguramos la compilación de los recursos frontend con el perfil de producción
RUN mvn clean package -Pproduction -DskipTests

FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY --from=build /app/target/server-monitor-1.0-SNAPSHOT.jar app.jar
# Agregamos punto de verificación de salud
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1
EXPOSE 8080
# Configuramos variables de entorno para optimizar la JVM en contenedores
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
# Usamos exec form para mejor manejo de señales
CMD ["java", "-jar", "app.jar"]