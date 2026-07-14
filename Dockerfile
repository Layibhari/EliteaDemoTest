# ============================================================================
# Stage 1: Build the JAR with Maven
# ============================================================================
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /workspace

# Copy Maven wrapper and pom.xml FIRST — separate layer for dependency cache
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Download dependencies as a distinct layer.
# If pom.xml doesn't change, this layer is cached — no re-download.
RUN ./mvnw -B dependency:go-offline

# Now copy source code and build.
# Source changes DON'T invalidate the deps layer above.
COPY src/ src/
RUN ./mvnw -B package -DskipTests

# ============================================================================
# Stage 2: Runtime image — minimal, no build tools
# ============================================================================
FROM eclipse-temurin:17-jre-jammy AS runtime
WORKDIR /app

# Create non-root user for runtime — never run Java as root in production
RUN groupadd --system spring && useradd --system --gid spring spring
USER spring:spring

# Copy ONLY the built JAR from the build stage
COPY --from=build --chown=spring:spring /workspace/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
