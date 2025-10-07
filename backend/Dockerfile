# ---- Build Stage ----
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

# copy pom.xml และ download dependencies ก่อน (cache)
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

RUN ./mvnw dependency:go-offline

# copy source code
COPY src ./src

# build jar
RUN ./mvnw clean package -DskipTests

# ---- Runtime Stage ----
FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

# copy jar มาจาก builder
COPY --from=builder /app/target/*.jar app.jar

# expose port ของ Spring Boot
EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]
