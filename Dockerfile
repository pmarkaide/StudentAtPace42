FROM gradle:8.5-jdk17

# Set working directory
WORKDIR /app

# Copy project files
COPY . /app/

# Ensure .env file is there (will be overridden by the env_file in docker-compose)
COPY .env /app/.env

# Create output directory
RUN mkdir -p /app/output

# Set volume for output
VOLUME /app/output

# Build the application (skip tests)
RUN gradle build -x test --no-daemon

# Command to run the application
CMD ["gradle", "run", "--no-daemon", "--console=plain"]