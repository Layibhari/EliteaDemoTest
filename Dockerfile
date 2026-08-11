# Stage 1: build the application with Maven
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy only the pom first so dependency downloads are cached
# across builds when only source code changes
COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: run it in a slim JRE, not the full JDK+Maven image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

HEALTHCHECK --interval=15s --timeout=5s --start-period=40s --retries=3 \
  CMD wget -qO- http://localhost:8080/ || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
