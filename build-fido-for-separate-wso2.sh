#!/bin/bash

echo "🔧 Building FIDO App for Separate WSO2 IS Instance"
echo "=================================================="
echo ""
echo "📋 Configuration updated for:"
echo "   ✅ WSO2 IS URL: https://ndanh.site:9443"
echo "   ✅ FIDO App URL: https://fido.18.143.154.126.nip.io"
echo "   ✅ Trusted Origins: ndanh.site, fido.18.143.154.126.nip.io"
echo ""
echo "🚀 Building and pushing FIDO app image..."

# Build locally first
echo "1. Building FIDO app locally..."
./gradlew clean build -x test

# Build and push Docker image
echo "2. Building and pushing Docker image..."
docker buildx build --platform linux/amd64,linux/arm64 -t ngoduyanh/fido-app:latest -f Dockerfile.mobile --push .

echo ""
echo "✅ FIDO app image updated successfully!"
echo ""
echo "📦 Image details:"
echo "   - Repository: ngoduyanh/fido-app"
echo "   - Tag: latest"
echo "   - Platforms: linux/amd64, linux/arm64"
echo ""
echo "🚀 To run FIDO app with separate WSO2 IS:"
echo "   docker-compose -f docker-compose-fido-only.yml up -d"
echo ""
echo "🔧 Configuration summary:"
echo "   - FIDO App: https://fido.18.143.154.126.nip.io"
echo "   - WSO2 IS: https://ndanh.site:9443"
echo "   - FIDO App connects to external WSO2 IS"
echo ""
echo "📝 Note: Make sure WSO2 IS is running on ndanh.site:9443"
echo "   and has the correct Service Provider configuration!"

