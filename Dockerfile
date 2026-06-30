# Build
FROM maven:3.9-eclipse-temurin-25 AS build

WORKDIR /workspace

COPY .mvn .mvn
COPY mvnw .
RUN chmod +x mvnw

COPY pom.xml ./
COPY src src

RUN ./mvnw -B -DskipTests package

# Run
FROM eclipse-temurin:25-jre AS runtime

WORKDIR /app

COPY --from=build /workspace/target/*.jar /app/app.jar

EXPOSE 8020

ENTRYPOINT ["java", "-jar", "/app/app.jar"]