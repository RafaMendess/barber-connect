# Dockerfile multi-stage para projeto Spring Boot (Java 21)
# Build stage: usa Maven com JDK 21 e pula os testes
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /workspace

# Copia apenas o necessário para aproveitar cache de dependências
COPY pom.xml mvnw mvnw.cmd ./
COPY .mvn .mvn
COPY src ./src

# Build do jar, com testes pulados
RUN mvn -B -DskipTests package

# Runtime stage: imagem mais leve com JRE 21
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copia o jar gerado do estágio de build
COPY --from=builder /workspace/target/*.jar app.jar

# Porta padrão do Spring Boot
EXPOSE 8080

# Inicia a aplicação
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
