# Etapa de build
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app
COPY . .
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

# Etapa de produção
FROM eclipse-temurin:21-jre
WORKDIR /app
# Certifique-se de que o JAR está sendo copiado do local correto
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
