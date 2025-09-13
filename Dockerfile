# Use a lightweight OpenJDK image
FROM eclipse-temurin:17-jdk-alpine

# Set build-time variables
ARG APP_NAME
ARG APP_VERSION
ARG PORT

# Set environment variables for runtime
ENV APP_NAME=${APP_NAME}
ENV APP_VERSION=${APP_VERSION}

# Create app directory
WORKDIR /app

# Copy JAR and entrypoint
COPY build-outputs/${APP_NAME}/libs/${APP_NAME}-${APP_VERSION}.jar ${APP_NAME}-${APP_VERSION}.jar
COPY entrypoint.sh /entrypoint.sh

# Make the script executable
RUN chmod +x /entrypoint.sh

# Expose the port your Spring Boot app runs on
EXPOSE ${PORT}

# Set the entrypoint
ENTRYPOINT ["/entrypoint.sh"]