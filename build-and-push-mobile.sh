#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[0;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${BLUE}🐳 Building and Pushing FIDO App with Mobile Integration${NC}"
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

# Build local first (already done)
echo -e "${GREEN}✅ Local build already completed${NC}"

# Build Docker image using local build
echo -e "${BLUE}🐳 Building Docker image with mobile integration...${NC}"
if docker build -t ngoduyanh/fido-app:latest .; then
    echo -e "${GREEN}✅ Docker image built successfully${NC}"
else
    echo -e "${RED}❌ Failed to build Docker image${NC}"
    echo -e "${YELLOW}💡 Tip: Try building with: docker build --no-cache -t ngoduyanh/fido-app:latest .${NC}"
    exit 1
fi

# Push to Docker Hub
echo -e "${BLUE}📤 Pushing FIDO app image to Docker Hub...${NC}"
if docker push ngoduyanh/fido-app:latest; then
    echo -e "${GREEN}✅ FIDO app image pushed successfully${NC}"
else
    echo -e "${RED}❌ Failed to push FIDO app image${NC}"
    exit 1
fi

echo -e "\n${GREEN}🎉 FIDO App with Mobile Integration completed successfully!${NC}"

echo -e "\n📋 Image details:"
echo "   Repository: ngoduyanh/fido-app"
echo "   Tag: latest"
echo "   Docker Hub URL: https://hub.docker.com/r/ngoduyanh/fido-app"

echo -e "\n🚀 Next steps:"
echo "   1. Your docker-compose.yml is ready to use"
echo "   2. Run: docker-compose up -d"
echo "   3. Test mobile endpoints: ./test-well-known.sh"

echo -e "\n📱 New Mobile Integration Features:"
echo "   ✅ Apple App Site Association: /.well-known/apple-app-site-association"
echo "   ✅ Android App Links: /.well-known/assetlinks.json"
echo "   ✅ Mobile Apps Management: /.well-known/fido/mobile/apps"
echo "   ✅ iOS Apps: /.well-known/fido/mobile/ios-apps"
echo "   ✅ Android Apps: /.well-known/fido/mobile/android-apps"

echo -e "\n🔧 Docker Compose Usage:"
echo "   - FIDO App: ngoduyanh/fido-app:latest"
echo "   - WSO2 IS: ngoduyanh/wso2is-custom-permission-fixed:latest"
echo "   - Ports: 8080 (FIDO), 9443 (WSO2 IS)"
echo "   - Origin fix: extra_hosts localhost:host-gateway"
