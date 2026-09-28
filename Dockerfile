# ==============================================================================
# Build Stage
# ==============================================================================
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app

# Copy Maven Wrapper and POM first to leverage Docker layer caching
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Ensure LF line endings and grant execute permissions on Maven wrapper
RUN sed -i 's/\r$//' ./mvnw && chmod +x ./mvnw

# Download dependencies (offline cache layer)
RUN ./mvnw dependency:go-offline -B || true

# Copy project source code
COPY src ./src

# Build production JAR (skipping test suite for fast, low-memory cloud container builds)
RUN ./mvnw clean package -DskipTests

# ==============================================================================
# Runtime Stage (Lightweight JRE)
# ==============================================================================
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Set container-aware JVM memory optimization (crucial for Render's 512MB RAM limit)
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"
ENV PORT=8080

# Create a non-root group and user for secure container execution
RUN addgroup --system spring && adduser --system --ingroup spring spring
USER spring:spring

# Copy built JAR artifact from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose HTTP port (Render dynamically overrides $PORT)
EXPOSE 8080

# Run Spring Boot application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT:-8080} -jar app.jar"]
