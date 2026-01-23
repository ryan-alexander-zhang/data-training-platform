FROM maven:3.9.8-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml ./
COPY adapter ./adapter
COPY app ./app
COPY domain ./domain
COPY infra ./infra
COPY start ./start
RUN mvn -pl start -am package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/start/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
