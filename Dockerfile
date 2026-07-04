# ==========================================
# Estágio 1: Build da Aplicação (Maven + JDK 21)
# ==========================================
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copia os arquivos do Maven Wrapper e pom.xml
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Dá permissão de execução ao script mvnw
RUN chmod +x mvnw

# Baixa as dependências principais (otimiza cache das camadas do Docker)
RUN ./mvnw dependency:go-offline -B || true

# Copia o código-fonte da aplicação
COPY src ./src

# Executa o empacotamento gerando o arquivo JAR (ignorando testes na etapa de build do container)
RUN ./mvnw clean package -DskipTests

# ==========================================
# Estágio 2: Execução Leve (JRE 21 Alpine)
# ==========================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Criação de usuário não-root para boas práticas de segurança (Railway/Docker)
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copia apenas o JAR gerado no estágio de build
COPY --from=build /app/target/*.jar app.jar

# Expõe a porta padrão 8080 (o Railway definirá a variável PORT se aplicável)
EXPOSE 8080

# Ponto de entrada executando a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]
