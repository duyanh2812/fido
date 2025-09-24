#!/bin/bash

# Script to build WSO2 IS image with proper permission fixes

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[0;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${BLUE}🐳 Building WSO2 IS image with proper permission fixes...${NC}"

# --- Configuration ---
DOCKER_HUB_USERNAME="ngoduyanh" # Replace with your Docker Hub username
IMAGE_NAME="wso2is-custom-permission-fixed"
IMAGE_TAG="latest"
FULL_IMAGE_NAME="$DOCKER_HUB_USERNAME/$IMAGE_NAME:$IMAGE_TAG"
WSO2_LOCALHOST_PATH="/Users/anhngo/Library/WSO2/wso2is-7.1.0" # Path to your local WSO2 IS installation
PLATFORMS="linux/amd64,linux/arm64" # Target platforms

# --- Pre-checks ---
if ! command -v docker &> /dev/null; then
    echo -e "${RED}❌ Error: Docker is not installed. Please install Docker and try again.${NC}"
    exit 1
fi

if ! docker info > /dev/null 2>&1; then
    echo -e "${RED}❌ Error: Docker is not running. Please start Docker and try again.${NC}"
    exit 1
fi

if [ ! -d "$WSO2_LOCALHOST_PATH" ]; then
    echo -e "${RED}❌ Error: WSO2 IS localhost installation not found at $WSO2_LOCALHOST_PATH${NC}"
    exit 1
fi
echo -e "${GREEN}✅ WSO2 IS localhost data found${NC}"

# Check if user is logged in to Docker Hub by testing a simple pull
echo "🔍 Checking Docker Hub login status..."
if docker pull hello-world > /dev/null 2>&1; then
    echo -e "${GREEN}✅ Docker can access Docker Hub${NC}"
else
    echo -e "${YELLOW}⚠️  Warning: You may not be logged in to Docker Hub.${NC}"
    echo "   Please run: docker login"
    echo "   Then run this script again."
    read -p "Continue anyway? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo -e "${RED}❌ Build cancelled${NC}"
        exit 1
    fi
fi

echo -e "${BLUE}📦 Building WSO2 IS image with proper permission fixes...${NC}"
echo "   Image: $FULL_IMAGE_NAME"
echo "   Platforms: $PLATFORMS"
echo "   Source: $WSO2_LOCALHOST_PATH"

# --- Build the image ---
# Temporarily copy localhost data to a subdirectory for Docker build context
TEMP_WSO2_DATA_DIR="wso2-data"
echo "Copying WSO2 IS localhost data to temporary directory for Docker build context..."
mkdir -p "$TEMP_WSO2_DATA_DIR/repository"
cp -R "$WSO2_LOCALHOST_PATH/repository/database" "$TEMP_WSO2_DATA_DIR/repository/"
cp -R "$WSO2_LOCALHOST_PATH/repository/conf" "$TEMP_WSO2_DATA_DIR/repository/"
cp -R "$WSO2_LOCALHOST_PATH/repository/logs" "$TEMP_WSO2_DATA_DIR/repository/"
cp -R "$WSO2_LOCALHOST_PATH/repository/deployment" "$TEMP_WSO2_DATA_DIR/repository/"
cp -R "$WSO2_LOCALHOST_PATH/repository/tenants" "$TEMP_WSO2_DATA_DIR/repository/"
cp -R "$WSO2_LOCALHOST_PATH/repository/components" "$TEMP_WSO2_DATA_DIR/repository/"
cp -R "$WSO2_LOCALHOST_PATH/repository/data" "$TEMP_WSO2_DATA_DIR/repository/"
cp -R "$WSO2_LOCALHOST_PATH/repository/resources" "$TEMP_WSO2_DATA_DIR/repository/"
echo -e "${GREEN}✅ WSO2 IS localhost data copied to $TEMP_WSO2_DATA_DIR${NC}"

# Build and push multi-platform image using buildx
if docker buildx build --platform "$PLATFORMS" -t "$FULL_IMAGE_NAME" -f Dockerfile.wso2-permission-fixed --push .; then
    echo -e "${GREEN}✅ WSO2 IS image with proper permission fixes built and pushed successfully${NC}"
    echo -e "${YELLOW}🏷️  Image tagged as:${NC}"
    echo "   - $FULL_IMAGE_NAME"
    echo "   - $DOCKER_HUB_USERNAME/$IMAGE_NAME:latest"
else
    echo -e "${RED}❌ Failed to build WSO2 IS image with proper permission fixes${NC}"
    rm -rf "$TEMP_WSO2_DATA_DIR" # Clean up temporary directory
    exit 1
fi

# Clean up temporary directory
rm -rf "$TEMP_WSO2_DATA_DIR"
echo "Cleaned up temporary WSO2 IS data directory."

echo -e "\n🎉 Successfully built and pushed WSO2 IS image with proper permission fixes to Docker Hub!\n"
echo -e "📋 Image details:"
echo "   - Repository: $DOCKER_HUB_USERNAME/$IMAGE_NAME"
echo "   - Tags: $IMAGE_TAG, latest"
echo "   - Platforms: $PLATFORMS"
echo "   - Docker Hub URL: https://hub.docker.com/r/$DOCKER_HUB_USERNAME/$IMAGE_NAME"
echo -e "\n🚀 This image includes proper permission fixes and should work perfectly!"
echo -e "\n📊 Included data:"
echo "   - Database: Users, roles, permissions, service providers"
echo "   - Configuration: deployment.toml, carbon.xml, user-mgt.xml"
echo "   - Deployed applications: All your deployed services"
echo "   - Logs: Historical log data"
echo "   - Tenants: Tenant configurations"
echo "   - Components: Component data"
echo "   - Resources: Resource files"
echo -e "\n🔧 Permission fixes:"
echo "   - Proper ownership: wso2carbon:wso2carbon"
echo "   - Correct permissions: 755 for repository, 777 for writable directories"
echo "   - Fixed at build time (not runtime)"
echo "   - No permission issues expected"

