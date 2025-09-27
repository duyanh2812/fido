#!/bin/bash

echo "🔍 Debug WebAuthn 'The operation is insecure' Error"
echo "=================================================="

echo "📋 This error typically occurs when:"
echo "   1. rp.id doesn't match the current domain"
echo "   2. HTTPS is required but not available"
echo "   3. Cross-origin issues"
echo "   4. Invalid rp.id format"
echo ""

echo "🚀 To debug the issue:"
echo "   1. Pull latest image: docker-compose pull"
echo "   2. Restart services: docker-compose up -d"
echo "   3. Open browser console and look for these debug logs:"
echo ""
echo "   🔍 Expected debug logs:"
echo "   - '🔍 rpId from server: [value from WSO2 IS]'"
echo "   - '🔍 rpId we will use: [final value being used]'"
echo "   - '🔍 Current origin: https://18.143.154.126:8080'"
echo "   - '🔍 Current hostname: 18.143.154.126'"
echo "   - '🔍 Final rp.id being used: [final rp.id]'"
echo "   - '🔍 Final rp.name being used: [rp.name]'"
echo ""

echo "🔧 Possible fixes based on debug output:"
echo "   - If rpId from server is null/undefined: WSO2 IS not returning rp.id"
echo "   - If rpId doesn't match origin: Domain mismatch issue"
echo "   - If rpId is IP instead of domain: Need to use domain name"
echo "   - If HTTPS not working: SSL certificate issue"
echo ""

echo "✅ Latest image with enhanced debug logging:"
echo "   - ngoduyanh/fido-app:latest"
echo "🎉 Ready for detailed debugging!"

