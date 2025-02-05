FROM maven:3.8-openjdk-17 as builder
WORKDIR /app
COPY producer/pom.xml producer/pom.xml
COPY pom.xml .
COPY producer/src producer/src
RUN mvn clean package -f producer/pom.xml

FROM openjdk:17-slim
COPY --from=builder /app/producer/target/iot-producer.jar /app/iot-producer.jar
CMD ["java", "-jar", "/app/iot-producer.jar"]