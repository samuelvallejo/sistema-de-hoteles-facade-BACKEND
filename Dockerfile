FROM maven:3.9.11-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B verify

FROM eclipse-temurin:17-jre
WORKDIR /app
RUN groupadd --system hotel && useradd --system --gid hotel hotel
COPY --from=build --chown=hotel:hotel /app/target/hotel-facade-1.0.0.jar app.jar
USER hotel
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
