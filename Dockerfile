# Use a lightweight Java 17 runtime
FROM eclipse-temurin:17-jre-alpine

# Create a non-root user
RUN addgroup -S spring && adduser -S spring -G spring

# Set the working directory
WORKDIR /app

# Copy the JAR downloaded into the target directory
COPY target/*.jar app.jar

# Set file ownership
RUN chown spring:spring app.jar

# Run the application as a non-root user
USER spring:spring

# Expose the Spring Boot application port
EXPOSE 8080

# Start the application
ENTRYPOINT ["java", "-jar", "app.jar"]