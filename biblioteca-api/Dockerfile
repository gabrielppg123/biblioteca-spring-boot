
# ============================
# ETAPA 1 - BUILD
# ============================

FROM maven:3.9-eclipse-temurin-21-noble AS build

WORKDIR /build

COPY pom.xml .
COPY src ./src

RUN mvn -B -DskipTests package


# ============================
# ETAPA 2 - EXECUCAO
# ============================

FROM eclipse-temurin:21-jre-noble

WORKDIR /app

# Permite executar sem privilegios de root
RUN chown 10001:10001 /app

COPY --from=build /build/target/*.jar /app/biblioteca-api.jar

USER 10001

EXPOSE 8080

ENV TZ=America/Sao_Paulo

ENTRYPOINT ["java", "-jar", "/app/biblioteca-api.jar"]
