FROM openjdk:17

WORKDIR /app

COPY target/consumer-0.0.1-SNAPSHOT.jar consumer-0.0.1-SNAPSHOT.jar

EXPOSE 8080

CMD ["java", "-jar", "consumer-0.0.1-SNAPSHOT.jar", "-r", "us-east-1"]
