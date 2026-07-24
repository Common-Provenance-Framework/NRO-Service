FROM eclipse-temurin:25-jdk@sha256:201fbb8886b2d273218aa3a192f0afbf7b5ff65ee8cc6ef47f5dce2171f013ea AS build

WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY src/ src/

RUN chmod +x mvnw
RUN ./mvnw -B package -DskipTests

FROM eclipse-temurin:26-jre-alpine-3.23@sha256:c4a22bec4f4368636abb9b6fe2b2350fd7fae1ec0d3bf43fcaae1be720c3bbd1 AS runtime

WORKDIR /app

COPY --from=build /workspace/target/*.jar /app/app.jar

EXPOSE 8020

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
