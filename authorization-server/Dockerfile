FROM maven:3.9-eclipse-temurin-21-noble AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn --batch-mode --no-transfer-progress -DskipTests package

FROM eclipse-temurin:21-jre-noble
WORKDIR /app
RUN chown 10001:10001 /app
COPY --from=build /build/target/*.jar /app/authorization-server.jar
USER 10001
ENV TZ=America/Sao_Paulo
EXPOSE 9000 9091
ENTRYPOINT ["java", "-jar", "/app/authorization-server.jar"]
