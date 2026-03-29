FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Copy the pre-built JAR from the host machine
COPY target/fleet_management_backend-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
