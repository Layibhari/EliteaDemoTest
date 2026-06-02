# ---- build stage ----
FROM eclipse-temurin:17-jdk-jammy AS builder
WORKDIR /build

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -q

COPY src/ src/
RUN ./mvnw -B package -DskipTests -q

# ---- runtime stage ----
FROM eclipse-temurin:17-jre-jammy

RUN groupadd --system petclinic && useradd --system --gid petclinic petclinic

WORKDIR /app
COPY --from=builder /build/target/spring-petclinic-*.jar app.jar
RUN chown petclinic:petclinic app.jar

USER petclinic

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
