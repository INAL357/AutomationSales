# ============ Build stage ============
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

# Download dependencies first (cached unless pom.xml changes)
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline

# Build the application
COPY src ./src
RUN ./mvnw -B -q clean package -DskipTests

# ============ Run stage ============
FROM eclipse-temurin:25-jre
WORKDIR /app

# Run as a non-root user
RUN groupadd --system spring && useradd --system --gid spring spring
COPY --from=build /app/target/*.jar app.jar
USER spring

EXPOSE 8080
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
