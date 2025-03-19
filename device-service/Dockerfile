FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY . /app
RUN mvn clean package -DskipTests -pl device-service -am

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/device-service/target/device-service-1.0-SNAPSHOT.jar app.jar
CMD ["java", "-jar", "app.jar"]