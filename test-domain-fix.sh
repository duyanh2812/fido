#!/bin/bash

echo "🔧 Testing Domain Fix for FIDO Registration"
echo "============================================"

echo "📋 The issue was:"
echo "   - WSO2 IS was configured with app_id = '18.143.154.126' (IP only)"
echo "   - FIDO App was sending appId = 'https://18.143.154.126:9443' (full URL)"
echo "   - This mismatch caused 'This is an invalid domain' error"
echo ""

echo "🔧 The fix:"
echo "   1. Updated FIDO_APP_ID to use IP only: '18.143.154.126'"
echo "   2. Updated Wso2Service to send appId = '18.143.154.126' (not full URL)"
echo "   3. This ensures WSO2 IS returns rp.id = '18.143.154.126'"
echo "   4. Frontend uses this rp.id which matches the domain '18.143.154.126:8080'"
echo ""

echo "🚀 To test the fix:"
echo "   1. Pull latest image: docker-compose pull"
echo "   2. Restart services: docker-compose up -d"
echo "   3. Check browser console for:"
echo "      - '🔍 rpId from server: 18.143.154.126'"
echo "      - '🔍 rpId we will use: 18.143.154.126'"
echo "   4. Try FIDO registration - should work now!"
echo ""

echo "🔍 Expected behavior:"
echo "   - Server sends appId = '18.143.154.126' to WSO2 IS"
echo "   - WSO2 IS returns rp.id = '18.143.154.126' in registration options"
echo "   - Frontend uses rp.id = '18.143.154.126' for WebAuthn API"
echo "   - Domain '18.143.154.126' is valid suffix of '18.143.154.126:8080'"
echo "   - No more 'This is an invalid domain' error"
echo ""

echo "✅ Fix deployed to Docker Hub: ngoduyanh/fido-app:latest"
echo "🎉 Ready for testing on AWS instance!"

