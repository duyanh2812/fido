#!/bin/bash

echo "Starting FIDO application with WSO2 IS 7.1.0 using Docker Compose..."

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "Error: Docker is not running. Please start Docker and try again."
    exit 1
fi

# Check if Docker Compose is available
if ! command -v docker-compose &> /dev/null; then
    echo "Error: docker-compose is not installed. Please install docker-compose and try again."
    exit 1
fi

# Build and start the services
echo "Building and starting services..."
docker-compose up --build -d

# Wait for services to be ready
echo "Waiting for services to be ready..."
echo "WSO2 IS will be available at: https://localhost:9443"
echo "FIDO Application will be available at: http://localhost:8080"

# Show logs
echo "Showing logs (press Ctrl+C to stop viewing logs, but services will continue running):"
docker-compose logs -f
