FROM openjdk:21-jdk

WORKDIR /app

COPY ./backend/target/stock-risk-backend-1.0.0.jar app.jar

# Expose the application port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]