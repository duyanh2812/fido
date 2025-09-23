# Multi-stage build for Spring Boot application
FROM gradle:8.5-jdk21 AS builder

# Set working directory
WORKDIR /app

# Copy gradle files
COPY build.gradle settings.gradle gradlew ./
COPY gradle/ gradle/

# Copy source code
COPY src/ src/

# Build the application
RUN ./gradlew clean build -x test

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

# Set working directory
WORKDIR /app

# Copy the built jar from builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Create SSL directory and copy keystore
RUN mkdir -p /app/ssl
COPY src/main/resources/ssl/keystore.p12 /app/ssl/keystore.p12

# Create a non-root user
RUN addgroup -S spring && adduser -S spring -G spring
RUN chown -R spring:spring /app
USER spring

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f https://localhost:8080/actuator/health -k || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]