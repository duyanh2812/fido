#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[0;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${BLUE}📱 Testing Mobile Integration Endpoints${NC}"
echo "=============================================="

# Check if services are running
if ! docker-compose ps | grep -q "Up"; then
    echo -e "${RED}❌ Services are not running. Please start them first:${NC}"
    echo "docker-compose up -d"
    exit 1
fi

echo -e "${GREEN}✅ Services are running${NC}"

# Test Apple App Site Association
echo -e "\n${BLUE}🍎 Testing Apple App Site Association:${NC}"
echo "URL: https://localhost:8080/.well-known/apple-app-site-association"
echo "Expected: {\"webcredentials\":{\"apps\":[\"XXXXXXXXXX.com.tymex.authentication.fido\"]}}"
echo "Response:"
curl -s -k https://localhost:8080/.well-known/apple-app-site-association | jq . 2>/dev/null || curl -s -k https://localhost:8080/.well-known/apple-app-site-association

# Test Android App Links
echo -e "\n${BLUE}🤖 Testing Android App Links:${NC}"
echo "URL: https://localhost:8080/.well-known/assetlinks.json"
echo "Expected: package_name: com.tymex.authentication.fido"
echo "Response:"
curl -s -k https://localhost:8080/.well-known/assetlinks.json | jq . 2>/dev/null || curl -s -k https://localhost:8080/.well-known/assetlinks.json

# Test FIDO Mobile Health
echo -e "\n${BLUE}📱 Testing FIDO Mobile Health:${NC}"
echo "URL: https://localhost:8080/.well-known/fido/mobile/health"
curl -s -k https://localhost:8080/.well-known/fido/mobile/health | jq . 2>/dev/null || curl -s -k https://localhost:8080/.well-known/fido/mobile/health

# Test FIDO Mobile Config
echo -e "\n${BLUE}⚙️ Testing FIDO Mobile Config:${NC}"
echo "URL: https://localhost:8080/.well-known/fido/mobile/config"
curl -s -k https://localhost:8080/.well-known/fido/mobile/config | jq . 2>/dev/null || curl -s -k https://localhost:8080/.well-known/fido/mobile/config

# Test FIDO Registration Options
echo -e "\n${BLUE}🔐 Testing FIDO Registration Options:${NC}"
echo "URL: https://localhost:8080/.well-known/fido/mobile/registration-options"
curl -s -k "https://localhost:8080/.well-known/fido/mobile/registration-options?userId=tymex-user&userName=tymex@example.com" | jq . 2>/dev/null || curl -s -k "https://localhost:8080/.well-known/fido/mobile/registration-options?userId=tymex-user&userName=tymex@example.com"

# Test FIDO Authentication Options
echo -e "\n${BLUE}🔑 Testing FIDO Authentication Options:${NC}"
echo "URL: https://localhost:8080/.well-known/fido/mobile/authentication-options"
curl -s -k "https://localhost:8080/.well-known/fido/mobile/authentication-options?userId=tymex-user" | jq . 2>/dev/null || curl -s -k "https://localhost:8080/.well-known/fido/mobile/authentication-options?userId=tymex-user"

echo -e "\n${GREEN}🎉 Mobile Integration Test Completed!${NC}"

echo -e "\n📋 Updated Package Information:"
echo "   🍎 iOS App ID: [AUTO-GENERATED-UUID].com.tymex.authentication.fido"
echo "   🤖 Android Package: com.tymex.authentication.fido"
echo "   📱 FIDO Service: https://localhost:8080/.well-known/fido/mobile/"

echo -e "\n🔧 iOS App Configuration:"
echo "   Associated Domains: webcredentials:localhost"
echo "   App ID: [Check response above for actual UUID]"

echo -e "\n🔧 Android App Configuration:"
echo "   Package Name: com.tymex.authentication.fido"
echo "   App Links: https://localhost"
