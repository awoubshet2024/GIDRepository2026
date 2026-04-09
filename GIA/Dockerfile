# Stage 1: Build the JAR using Maven and Java 21
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
# Copy the pom and source code
COPY pom.xml .
COPY src ./src
# Build the JAR inside the container
RUN mvn clean package -DskipTests

# Stage 2: Create the final small image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Copy the JAR from the 'build' stage
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "--enable-preview","-jar", "app.jar"]