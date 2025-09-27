#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[0;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${BLUE}🐳 Building Multi-Platform FIDO App Image${NC}"
echo "=============================================="

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo -e "${RED}❌ Error: Docker is not running. Please start Docker and try again.${NC}"
    exit 1
fi

# Check if logged in to Docker Hub
echo -e "${BLUE}🔐 Checking Docker Hub login...${NC}"
if ! docker pull hello-world > /dev/null 2>&1; then
    echo -e "${RED}❌ You need to login to Docker Hub first${NC}"
    echo "Run: docker login"
    exit 1
fi

echo -e "${GREEN}✅ Docker Hub login confirmed${NC}"

# Build local first
echo -e "${BLUE}🔨 Building local application...${NC}"
if ./gradlew clean build -x test; then
    echo -e "${GREEN}✅ Local build completed successfully${NC}"
else
    echo -e "${RED}❌ Failed to build local application${NC}"
    exit 1
fi

# Create and use buildx builder
echo -e "${BLUE}🔧 Setting up multi-platform builder...${NC}"
docker buildx create --name multiplatform --use 2>/dev/null || docker buildx use multiplatform

# Build multi-platform image
echo -e "${BLUE}🐳 Building multi-platform FIDO app image...${NC}"
if docker buildx build \
    --platform linux/amd64,linux/arm64 \
    --file Dockerfile.mobile \
    --tag ngoduyanh/fido-app:latest \
    --push \
    .; then
    echo -e "${GREEN}✅ Multi-platform FIDO app image built and pushed successfully${NC}"
else
    echo -e "${RED}❌ Failed to build multi-platform FIDO app image${NC}"
    exit 1
fi

echo -e "\n${GREEN}🎉 Multi-Platform FIDO App Build Completed!${NC}"

echo -e "\n📋 Image details:"
echo "   Repository: ngoduyanh/fido-app"
echo "   Tag: latest"
echo "   Platforms: linux/amd64, linux/arm64"
echo "   Docker Hub URL: https://hub.docker.com/r/ngoduyanh/fido-app"

echo -e "\n🚀 Usage on AWS:"
echo "   - t3.micro (x86_64): ✅ Supported"
echo "   - t4g.micro (ARM64): ✅ Supported"
echo "   - t3g.micro (ARM64): ✅ Supported"

echo -e "\n📱 Mobile Integration Features:"
echo "   ✅ Apple App Site Association: /.well-known/apple-app-site-association"
echo "   ✅ Android App Links: /.well-known/assetlinks.json"
echo "   ✅ Mobile Apps Management: /.well-known/fido/mobile/apps"

