FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src

# Install git
RUN apt-get update && apt-get install -y git && rm -rf /var/lib/apt/lists/*

# Clone if missing, else pull latest
RUN if [ ! -d ".git" ]; then \
      git clone https://github.com/dev-kartik4/springboot-microservices-ecom.git . ; \
    else \
      git pull ; \
    fi \

RUN mvn -q -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
EXPOSE 8761
LABEL maintainer="kartikcodes.dev"

COPY --from=builder /app/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
