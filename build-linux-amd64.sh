#!/bin/bash

# Build FIDO App Docker image for linux/amd64 platform and push to Docker Hub
# Usage: ./build-linux-amd64.sh

echo "Building FIDO App Docker image for linux/amd64 platform..."

# Build and push for linux/amd64 only
docker buildx build --platform linux/amd64 -t ngoduyanh/fido-app:latest --push .

if [ $? -eq 0 ]; then
    echo "✅ Build and push completed successfully!"
    echo "Image: ngoduyanh/fido-app:latest"
    echo "Platform: linux/amd64"
else
    echo "❌ Build failed!"
    exit 1
fi

