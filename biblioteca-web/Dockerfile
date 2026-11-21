FROM maven:3.9-eclipse-temurin-21-noble AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package
FROM eclipse-temurin:21-jre-noble
WORKDIR /app
RUN chown 10001:10001 /app
COPY --from=build /build/target/*.jar /app/biblioteca-web.jar
USER 10001
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/biblioteca-web.jar"]
