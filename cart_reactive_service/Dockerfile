FROM openjdk:17
EXPOSE 8550
LABEL authors="kartikcodes.dev"
ADD target/springboot-cart-reactive-service-docker.jar springboot-cart-reactive-service-docker.jar
ENTRYPOINT ["java","-jar","springboot-cart-reactive-service-docker.jar"]