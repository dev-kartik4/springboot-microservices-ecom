FROM openjdk:17
EXPOSE 8110
LABEL authors="kartikcodes.dev"
ADD target/springboot-customer-reactive-service-docker.jar springboot-customer-reactive-service-docker.jar
ENTRYPOINT ["java","-jar","springboot-customer-reactive-service-docker.jar"]