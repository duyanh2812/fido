#!/bin/bash

echo "Testing Docker Compose setup for FIDO application..."

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Error: Docker is not running. Please start Docker and try again."
    exit 1
fi

echo "✅ Docker is running"

# Check if Docker Compose is available
if ! command -v docker-compose &> /dev/null; then
    echo "❌ Error: docker-compose is not installed. Please install docker-compose and try again."
    exit 1
fi

echo "✅ Docker Compose is available"

# Validate docker-compose.yml
echo "🔍 Validating docker-compose.yml..."
if docker-compose config > /dev/null 2>&1; then
    echo "✅ docker-compose.yml is valid"
else
    echo "❌ docker-compose.yml has errors"
    exit 1
fi

# Check if ports are available
echo "🔍 Checking port availability..."

if lsof -i :8080 > /dev/null 2>&1; then
    echo "⚠️  Warning: Port 8080 is already in use"
else
    echo "✅ Port 8080 is available"
fi

if lsof -i :9443 > /dev/null 2>&1; then
    echo "⚠️  Warning: Port 9443 is already in use"
else
    echo "✅ Port 9443 is available"
fi

echo ""
echo "🚀 Ready to start services!"
echo "Run: ./start-docker.sh"
echo "Or: docker-compose up --build -d"
echo ""
echo "Services will be available at:"
echo "- WSO2 IS: https://localhost:9443"
echo "- FIDO App: http://localhost:8080"
