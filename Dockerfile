FROM eclipse-temurin:25-jdk@sha256:8c0a84ea11c8f6ed52600fc19f1040121f2a162998e9f50a5faebbbad9172dcc AS build

WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY src/ src/

RUN chmod +x mvnw
RUN ./mvnw -B package -DskipTests

FROM eclipse-temurin:27-jre-alpine-3.24@sha256:9d76de111573e77ac415d8344271a2e57dd19bd59c48725fa3bac38c101bd0ba AS runtime

WORKDIR /app

COPY --from=build /workspace/target/*.jar /app/app.jar

EXPOSE 8020

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
