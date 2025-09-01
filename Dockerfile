# Step 1: Use a lightweight JDK image
FROM openjdk:17-jdk-slim

# Step 2: Set working directory inside container
WORKDIR /app


# Step 3: Copy built JAR into container
COPY hotelbooking-0.0.1-SNAPSHOT.jar app.jar


# Step 4: Expose port (default Spring Boot port)
EXPOSE 8080

# Step 5: Run the JAR
ENTRYPOINT ["java", "-jar", "app.jar"]
