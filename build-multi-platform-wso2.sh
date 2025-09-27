#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[0;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${BLUE}🐳 Building Multi-Platform WSO2 IS Image${NC}"
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

# Check if WSO2 data exists
if [ ! -d "wso2-data" ]; then
    echo -e "${RED}❌ WSO2 data directory not found${NC}"
    echo "Please ensure wso2-data directory exists with your WSO2 IS data"
    exit 1
fi

# Create and use buildx builder
echo -e "${BLUE}🔧 Setting up multi-platform builder...${NC}"
docker buildx create --name multiplatform --use 2>/dev/null || docker buildx use multiplatform

# Build multi-platform WSO2 IS image
echo -e "${BLUE}🐳 Building multi-platform WSO2 IS image...${NC}"
if docker buildx build \
    --platform linux/amd64,linux/arm64 \
    --file Dockerfile.wso2-permission-fixed \
    --tag ngoduyanh/wso2is-custom-permission-fixed:latest \
    --push \
    .; then
    echo -e "${GREEN}✅ Multi-platform WSO2 IS image built and pushed successfully${NC}"
else
    echo -e "${RED}❌ Failed to build multi-platform WSO2 IS image${NC}"
    exit 1
fi

echo -e "\n${GREEN}🎉 Multi-Platform WSO2 IS Build Completed!${NC}"

echo -e "\n📋 Image details:"
echo "   Repository: ngoduyanh/wso2is-custom-permission-fixed"
echo "   Tag: latest"
echo "   Platforms: linux/amd64, linux/arm64"
echo "   Docker Hub URL: https://hub.docker.com/r/ngoduyanh/wso2is-custom-permission-fixed"

echo -e "\n🚀 Usage on AWS:"
echo "   - t3.micro (x86_64): ✅ Supported"
echo "   - t4g.micro (ARM64): ✅ Supported"
echo "   - t3g.micro (ARM64): ✅ Supported"

echo -e "\n🔧 Features:"
echo "   ✅ Custom WSO2 IS with your localhost data"
echo "   ✅ Permission fixes for ARM64 and AMD64"
echo "   ✅ Multi-platform support"

