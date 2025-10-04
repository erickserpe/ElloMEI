# Multi-stage build for optimized image size

# Stage 1: Build the application
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copy pom.xml and source code
COPY pom.xml .
COPY src ./src

# Build application (Maven will download dependencies automatically)
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime image
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Create non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring

# Create directories
RUN mkdir -p /app/uploads /var/log/scf-mei && \
    chown -R spring:spring /app /var/log/scf-mei

# Copy the built JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Change ownership of the JAR
RUN chown spring:spring app.jar

# Switch to non-root user
USER spring:spring

# Expose port 8080
EXPOSE 8080

# Environment variable for JVM options (can be overridden)
ENV JAVA_OPTS=""

# Run the application with JAVA_OPTS support
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

