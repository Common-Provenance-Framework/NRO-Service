FROM eclipse-temurin:25-jdk@sha256:00a2fe7ef4aacaa1f94ada99110eaf1072b6eddd04cbbfc79a7b1856c7f5c71c AS build

WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY src/ src/

RUN chmod +x mvnw
RUN ./mvnw -B package -DskipTests

FROM eclipse-temurin:25-jre-alpine-3.23@sha256:cdd967aa55f1d0175ebe57245e4450292e6e6dd185dce73f93580598934128aa AS runtime

WORKDIR /app

COPY --from=build /workspace/target/*.jar /app/app.jar

EXPOSE 8020

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
