# # Step 1: Use a lightweight JDK image
# FROM openjdk:17-jdk-slim

# # Step 2: Set working directory inside container
# WORKDIR /app


# # Step 3: Copy built JAR into container
# COPY target/hotelbooking-0.0.1-SNAPSHOT.jar app.jar


# # Step 4: Expose port (default Spring Boot port)
# EXPOSE 8080

# # Step 5: Run the JAR
# ENTRYPOINT ["java", "-jar", "app.jar"]


# -------- Stage 1: Build the JAR using Maven --------
FROM maven:3.9.6-eclipse-temurin-17 AS build

# Set working directory
WORKDIR /app

# Copy pom.xml and download dependencies first (caching advantage)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Package the application (skip tests for faster build)
RUN mvn clean package -DskipTests

# -------- Stage 2: Run the built JAR --------
FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy JAR from build stage
COPY --from=build /app/target/hotelbooking-0.0.1-SNAPSHOT.jar app.jar

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
