#!/bin/bash

# Create output directory if it doesn't exist
mkdir -p output

# Stop existing containers if running
docker-compose down

# Build and run the Docker container with attached output
docker-compose up --build
