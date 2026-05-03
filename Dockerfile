FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
# The maven build creates the jar in the target directory
COPY target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar", "--server.port=8081"]
