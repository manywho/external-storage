FROM maven:3-jdk-10-slim AS build
WORKDIR /usr/src/app
COPY pom.xml pom.xml
RUN mvn dependency:resolve
COPY src src
RUN mvn clean package

FROM openjdk:10-jre-slim
EXPOSE 8080
COPY --from=build /usr/src/app/target/external-storage.jar /usr/src/app/target/external-storage.jar
CMD ["java", "-jar", "/usr/src/app/target/external-storage.jar"]
