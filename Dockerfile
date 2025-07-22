FROM amazoncorretto:21-alpine3.22

WORKDIR /app

COPY gradlew .
COPY gradle ./gradle

COPY build.gradle .
COPY settings.gradle .

RUN chmod +x ./gradlew

RUN ./gradlew dependencies

COPY src ./src

RUN ./gradlew build -x test

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "build/libs/spring-concurrency-0.0.1-SNAPSHOT.jar"]
