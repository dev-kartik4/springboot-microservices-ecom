FROM openjdk:17
EXPOSE 8761
LABEL authors="kartikcodes.dev"
RUN mvn -q -DskipTests package
COPY target/springboot-eureka-reactive-service-docker.jar springboot-eureka-reactive-service-docker.jar
ENTRYPOINT ["java","-jar","springboot-eureka-reactive-service-docker.jar"]