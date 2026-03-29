FROM openjdk:21-jdk

WORKDIR 

COPY ./app/target/employee-directory.jar .

# Expose the application port (change if necessary)
EXPOSE 8080:8080

# Run the application
ENTRYPOINT ["java", "-jar", "stock-risk-dashboard-1.0.0.jar"]