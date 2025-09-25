#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[0;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${BLUE}📱 Testing .well-known Mobile Integration Endpoints${NC}"
echo "=============================================="

# Check if application is running
if ! curl -s -k https://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo -e "${RED}❌ Application is not running. Please start it first:${NC}"
    echo "Run the application in IntelliJ or: ./gradlew bootRun"
    exit 1
fi

echo -e "${GREEN}✅ Application is running${NC}"

# Test Apple App Site Association
echo -e "\n${BLUE}🍎 Testing Apple App Site Association:${NC}"
echo "URL: https://localhost:8080/.well-known/apple-app-site-association"
echo "Expected: {\"webcredentials\":{\"apps\":[\"C0289CB5B6.com.tymex.authentication.fido\",\"A1B2C3D4E5.com.tymex.authentication.fido.staging\"]}}"
echo "Response:"
curl -s -k https://localhost:8080/.well-known/apple-app-site-association | jq . 2>/dev/null || curl -s -k https://localhost:8080/.well-known/apple-app-site-association

# Test Android App Links
echo -e "\n${BLUE}🤖 Testing Android App Links:${NC}"
echo "URL: https://localhost:8080/.well-known/assetlinks.json"
echo "Expected: Multiple Android apps from config"
echo "Response:"
curl -s -k https://localhost:8080/.well-known/assetlinks.json | jq . 2>/dev/null || curl -s -k https://localhost:8080/.well-known/assetlinks.json

# Test Mobile Apps Management
echo -e "\n${BLUE}📱 Testing Mobile Apps Management:${NC}"
echo "URL: https://localhost:8080/.well-known/fido/mobile/apps"
curl -s -k https://localhost:8080/.well-known/fido/mobile/apps | jq . 2>/dev/null || curl -s -k https://localhost:8080/.well-known/fido/mobile/apps

# Test iOS Apps
echo -e "\n${BLUE}🍎 Testing iOS Apps:${NC}"
echo "URL: https://localhost:8080/.well-known/fido/mobile/ios-apps"
curl -s -k https://localhost:8080/.well-known/fido/mobile/ios-apps | jq . 2>/dev/null || curl -s -k https://localhost:8080/.well-known/fido/mobile/ios-apps

# Test Android Apps
echo -e "\n${BLUE}🤖 Testing Android Apps:${NC}"
echo "URL: https://localhost:8080/.well-known/fido/mobile/android-apps"
curl -s -k https://localhost:8080/.well-known/fido/mobile/android-apps | jq . 2>/dev/null || curl -s -k https://localhost:8080/.well-known/fido/mobile/android-apps

echo -e "\n${GREEN}🎉 .well-known Mobile Integration Test Completed!${NC}"

echo -e "\n📋 Available .well-known Endpoints:"
echo "   🍎 Apple App Site Association: https://localhost:8080/.well-known/apple-app-site-association"
echo "   🤖 Android App Links: https://localhost:8080/.well-known/assetlinks.json"
echo "   📱 Mobile Apps: https://localhost:8080/.well-known/fido/mobile/apps"
echo "   🍎 iOS Apps: https://localhost:8080/.well-known/fido/mobile/ios-apps"
echo "   🤖 Android Apps: https://localhost:8080/.well-known/fido/mobile/android-apps"

echo -e "\n🔧 Mobile App Integration:"
echo "   ✅ iOS: Configure Associated Domains with 'webcredentials:localhost'"
echo "   ✅ Android: Add App Links for 'https://localhost'"
echo "   ✅ Use existing FIDO APIs: /fido/registration-options, /fido/register, /fido/authentication-options, /fido/authenticate"

echo -e "\n📝 How to add new mobile apps:"
echo "   1. Edit src/main/resources/mobile-apps.yml"
echo "   2. Add new iOS/Android app entries"
echo "   3. Restart application"
echo "   4. Test with this script"
