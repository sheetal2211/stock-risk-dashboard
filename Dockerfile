FROM eclipse-temurin:21-jdk-jammy

WORKDIR /backend

COPY ./backend/target/stock-risk-backend-1.0.0.jar .

# Expose the application port (change if necessary)
EXPOSE 8080:8080

# Run the application
ENTRYPOINT ["java", "-jar", "stock-risk-backend-1.0.0.jar"]