# =========================================================
# Stage 1: Build stage
# =========================================================
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom.xml and source code
COPY pom.xml .
COPY src ./src

# Compile and package the application
RUN mvn clean package -DskipTests

# =========================================================
# Stage 2: Runtime stage
# =========================================================
FROM eclipse-temurin:17-jre
WORKDIR /app

# Install LibreOffice headless for PDF report generation
RUN apt-get update && \
    apt-get install -y --no-install-recommends libreoffice && \
    rm -rf /var/lib/apt/lists/*

# Copy the built jar from the build stage
COPY --from=build /app/target/faculty-feedback-system.jar app.jar

# Expose the application port
EXPOSE 8080

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
