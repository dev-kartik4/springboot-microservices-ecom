# Stage 1 - build
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests package

# Stage 2 - runtime
FROM eclipse-temurin:17-jre
WORKDIR /app
EXPOSE 8761
LABEL maintainer="kartikcodes.dev"

COPY --from=builder /app/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
