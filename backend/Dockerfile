# # ---- Build Stage ----
# FROM maven:3.9.6-eclipse-temurin-21 AS builder

# WORKDIR /app

# # copy pom.xml และ download dependencies ก่อน (cache)
# COPY pom.xml .
# COPY mvnw .
# COPY .mvn .mvn

# RUN ./mvnw dependency:go-offline

# # copy source code
# COPY src ./src

# # build jar
# RUN ./mvnw clean package -DskipTests

# # ---- Runtime Stage ----
# FROM eclipse-temurin:21-jdk-jammy

# WORKDIR /app

# # copy jar มาจาก builder
# COPY --from=builder /app/target/*.jar app.jar

# # expose port ของ Spring Boot
# EXPOSE 8080

# ENTRYPOINT ["java","-jar","app.jar"]


# Stage 1: Build the application using Maven
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
# Add execute permissions to the Maven wrapper
RUN chmod +x ./mvnw
RUN ./mvnw dependency:go-offline

# Copy source code
COPY src ./src

# Package the application
RUN ./mvnw package -DskipTests
# RUN ./mvnw package -Dmaven.test.skip=true

# Stage 2: Create a minimal runtime image
FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app
# Copy the built JAR from the builder stage
COPY --from=builder /app/target/*.jar app.jar
# Expose the port your app runs on
EXPOSE 8080
# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]