FROM maven:3.8-openjdk-17 as builder
WORKDIR /app
COPY processor/pom.xml processor/pom.xml
COPY pom.xml .
COPY processor/src processor/src
RUN mvn clean package -f processor/pom.xml

FROM openjdk:17-slim
COPY --from=builder /app/processor/target/iot-processor.jar /app/iot-processor.jar
CMD ["java", "-jar", "/app/iot-processor.jar"]