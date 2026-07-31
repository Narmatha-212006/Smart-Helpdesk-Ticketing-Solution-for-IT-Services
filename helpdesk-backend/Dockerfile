# Stage 1: Build the application using maven
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy the pom.xml and source code
COPY pom.xml .
COPY src ./src

# Package the application skipping tests to speed up the build in the cloud
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the generated jar from the build stage
COPY --from=build /app/target/backend-1.0.0-SNAPSHOT.jar app.jar

# Expose the port (Render sets $PORT dynamically)
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
