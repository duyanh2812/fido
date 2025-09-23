#!/bin/bash

# Script to update WSO2 IS configuration in Docker

WSO2_CONFIG_PATH="/Users/anhngo/Library/WSO2/wso2is-7.1.0/repository/conf/deployment.toml"
DOCKER_COMPOSE_FILE="docker-compose.yml"

echo "🔧 Updating WSO2 IS configuration in Docker..."

# Check if the config file exists
if [ ! -f "$WSO2_CONFIG_PATH" ]; then
    echo "❌ Error: WSO2 config file not found at: $WSO2_CONFIG_PATH"
    echo "Please check the path and try again."
    exit 1
fi

echo "✅ Found WSO2 config file at: $WSO2_CONFIG_PATH"

# Check if docker-compose is running
if docker-compose ps | grep -q "wso2is"; then
    echo "🔄 WSO2 IS container is running. Restarting to apply new configuration..."
    docker-compose restart wso2is
    echo "✅ WSO2 IS restarted with new configuration"
else
    echo "ℹ️  WSO2 IS container is not running. Configuration will be applied when you start the services."
fi

echo ""
echo "📋 Current WSO2 IS configuration includes:"
echo "- FIDO WebAuthn settings"
echo "- CORS configuration for localhost:8080 and localhost:9443"
echo "- Trusted origins for FIDO authentication"
echo ""
echo "🚀 To start services with the updated configuration:"
echo "   ./start-docker.sh"
echo "   or"
echo "   docker-compose up -d"
