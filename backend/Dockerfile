# syntax=docker/dockerfile:1

# ---- Build stage: compila o JAR com Maven ----
# Usa a imagem oficial do Maven (evita depender do mvnw / line-endings no build).
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Baixa as dependências primeiro para aproveitar o cache de camadas do Docker.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# Compila e empacota (testes rodam localmente / no CI, não na imagem de deploy).
COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---- Runtime stage: imagem enxuta só com o JRE ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# O Render injeta a porta via variável de ambiente PORT; a aplicação a lê em
# server.port (application.yml). 8081 é só o default local documentado.
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
