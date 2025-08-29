FROM openjdk:21-jdk-slim

# Instalar dependências do sistema
RUN apt-get update && apt-get install -y \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Criar usuário não-root
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Configurar diretório de trabalho
WORKDIR /app

# Copiar arquivos de configuração Maven
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

# Baixar dependências (cache layer)
RUN ./mvnw dependency:go-offline -B

# Copiar código fonte
COPY src ./src

# Compilar aplicação
RUN ./mvnw clean package -DskipTests

# Criar diretório para arquivos do agente
RUN mkdir -p /tmp/agent-files && chown -R appuser:appuser /tmp/agent-files

# Mudar para usuário não-root
USER appuser

# Expor porta
EXPOSE 8080

# Configurar healthcheck
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Comando de execução
ENTRYPOINT ["java", "-jar", "target/spring-ai-agent-1.0.0-SNAPSHOT.jar"]
