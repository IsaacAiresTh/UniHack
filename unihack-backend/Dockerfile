FROM openjdk:17-jdk-slim

WORKDIR /app

COPY pom.xml .

RUN mvn dependency:go-offline

COPY src ./src
COPY mvnw .
COPY mvnw.cmd .

RUN chmod +x ./mvnw && ./mvnw clean package

CMD ["java", "-jar", "target/*.jar"]