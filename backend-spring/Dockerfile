## Stage 1: Build
#FROM maven:3.9.5-eclipse-temurin-21 AS build
#COPY . .
#RUN mvn clean package -DskipTests
#
## Stage 2: Runtime - Oracle OpenJDK 21
#FROM openjdk:21-jdk-slim
#
#COPY --from=build /target/smartqr-0.0.1-SNAPSHOT.jar smartqr.jar
#EXPOSE 8080
#ENTRYPOINT ["jala", "-jar", "demo.jar"]

# Stage 1: Build
#FROM maven:3.9.5-eclipse-temurin-21 AS build
#
#WORKDIR /app
#
## Copy pom.xml and download dependencies (cached layer)
#COPY pom.xml .
#RUN mvn dependency:go-offline -B
#
## Copy source code and build
#COPY src ./src
#RUN mvn clean package -DskipTests
#
## Stage 2: Runtime - Oracle OpenJDK 21
#FROM openjdk:21-jdk-slim
#
#WORKDIR /app
#
## Install wget for healthcheck
#RUN apt-get update && \
#    apt-get install -y --no-install-recommends wget && \
#    apt-get clean && \
#    rm -rf /var/lib/apt/lists/*
#
## Create non-root user
#RUN groupadd -r spring && useradd -r -g spring spring
#
## Copy JAR from build stage
#COPY --from=build --chown=spring:spring /app/target/backend-spring-0.0.1-SNAPSHOT.jar backend-spring.jar
#
## Switch to non-root user
#USER spring:spring
#
## Expose port
#EXPOSE 8080
#
## Health check
#HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
#  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/health || exit 1
#
## Run application (FIXED: was "jala" and "demo.jar")
#ENTRYPOINT ["java", "-jar", "backend-spring.jar"]

# Stage 1: Build
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy pom.xml and download dependencies (cached layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime - Eclipse Temurin 21 (Official OpenJDK)
FROM eclipse-temurin:21-jre

WORKDIR /app

# Install curl for healthcheck (lighter than wget)
RUN apt-get update && \
    apt-get install -y --no-install-recommends curl && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Create non-root user
RUN groupadd -r spring && useradd -r -g spring spring

# Copy JAR from build stage
COPY --from=build --chown=spring:spring /app/target/*.jar app.jar

# Switch to non-root user
USER spring:spring

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl --fail http://localhost:8080/api/health || exit 1

# Run application
ENTRYPOINT ["java", "-Xmx512m", "-Xms256m", "-jar", "app.jar"]
