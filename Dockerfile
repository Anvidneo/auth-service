# Multi-stage Dockerfile for Bank Dugongo Auth Service

# ---- Build Stage ----
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

# Copy Gradle wrapper and build files
COPY gradlew build.gradle settings.gradle /app/
COPY gradle /app/gradle

# Copy source code
COPY src /app/src

#Copy local application properties
COPY src/main/resources/application-local.properties /app/application-local.properties

# Build the .jar file
RUN ./gradlew bootJar --no-daemon

# ---- Run Stage ----
FROM eclipse-temurin:25-jre
WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
