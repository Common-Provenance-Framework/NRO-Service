FROM eclipse-temurin:25-jdk@sha256:dcf835e52330939b6c9f90ecab8aafcbcaa8fbf48423db44de884cf978c10144 AS build

WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY src/ src/

RUN chmod +x mvnw
RUN ./mvnw -B package -DskipTests

FROM eclipse-temurin:26-jre-alpine-3.24@sha256:2c984601c59ac93f97947ce66d03df25c2e16bed97a28289fbc0ab157087f1b5 AS runtime

WORKDIR /app

COPY --from=build /workspace/target/*.jar /app/app.jar

EXPOSE 8020

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
