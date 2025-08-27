FROM openjdk:17
EXPOSE 8330
LABEL authors="kartikcodes.dev"
ADD target/springboot-inventory-reactive-service-docker.jar springboot-inventory-reactive-service-docker.jar
ENTRYPOINT ["java","-jar","springboot-inventory-reactive-service-docker.jar"]