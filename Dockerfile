# Build stage
FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Install curl for health check
RUN apk add --no-cache curl

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring

# Copy JAR from build stage
COPY --from=builder /app/target/*.jar app.jar

# Change file ownership
RUN chown spring:spring /app/app.jar
USER spring

# Environment variables
ENV SPRING_PROFILES_ACTIVE=docker
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]