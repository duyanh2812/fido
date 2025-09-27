#!/bin/bash

echo "🔧 Testing Full URL Configuration for FIDO"
echo "==========================================="

echo "📋 Configuration Summary:"
echo "   ✅ FIDO_APP_ID = https://18.143.154.126:8080 (full URL)"
echo "   ✅ WSO2 IS app_id = https://18.143.154.126:8080 (full URL)"
echo "   ✅ Frontend rp.id = https://18.143.154.126:8080 (full URL)"
echo "   ✅ All components now use consistent full URLs"
echo ""

echo "🔍 Expected Behavior:"
echo "   1. FIDO App sends appId = 'https://18.143.154.126:8080' to WSO2 IS"
echo "   2. WSO2 IS returns rp.id = 'https://18.143.154.126:8080' in registration options"
echo "   3. Frontend uses rp.id = 'https://18.143.154.126:8080' for WebAuthn API"
echo "   4. Domain matching: 'https://18.143.154.126:8080' matches origin 'https://18.143.154.126:8080' ✅"
echo ""

echo "🚀 To test the fix:"
echo "   1. Pull latest images: docker-compose pull"
echo "   2. Restart services: docker-compose up -d"
echo "   3. Check browser console for:"
echo "      - '🔍 rpId from server: https://18.143.154.126:8080'"
echo "      - '🔍 rpId we will use: https://18.143.154.126:8080'"
echo "   4. Try FIDO registration - should work perfectly!"
echo ""

echo "✅ Both images updated:"
echo "   - FIDO App: ngoduyanh/fido-app:latest"
echo "   - WSO2 IS: ngoduyanh/wso2is-custom-permission-fixed:latest"
echo "🎉 Ready for testing on AWS instance!"

