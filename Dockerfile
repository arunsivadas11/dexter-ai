# Use lightweight JDK
FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

# Copy Maven wrapper files
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Download dependencies
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline

# Copy source
COPY src src

# Build app
RUN ./mvnw clean package -DskipTests

# Expose port (Render uses 8080 by default)
EXPOSE 8080

# Run app
CMD ["java", "-jar", "target/*.jar"]