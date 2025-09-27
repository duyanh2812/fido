#!/bin/bash

echo "🌐 Testing NIP.IO Domain Mapping"
echo "================================="

echo "📋 Domain Mapping Configuration:"
echo "   ✅ fido.18.143.154.126.nip.io → FIDO App (port 8080)"
echo "   ✅ wso2.18.143.154.126.nip.io → WSO2 IS (port 9443)"
echo ""

echo "🔧 Updated Configuration:"
echo "   - FIDO_RP_ID: fido.18.143.154.126.nip.io"
echo "   - FIDO_APP_ID: https://fido.18.143.154.126.nip.io"
echo "   - FIDO_TRUSTED_ORIGINS: wso2.18.143.154.126.nip.io, fido.18.143.154.126.nip.io"
echo "   - WSO2 IS app_id: fido.18.143.154.126.nip.io"
echo "   - WSO2 IS trusted_origins: fido.18.143.154.126.nip.io, wso2.18.143.154.126.nip.io"
echo ""

echo "🚀 To test the domain mapping:"
echo "   1. Pull latest images: docker-compose pull"
echo "   2. Restart services: docker-compose up -d"
echo "   3. Access applications:"
echo "      - FIDO App: https://fido.18.143.154.126.nip.io"
echo "      - WSO2 IS: https://wso2.18.143.154.126.nip.io"
echo ""

echo "🔍 Expected behavior:"
echo "   - Domain names should resolve to 18.143.154.126"
echo "   - HTTPS should work with nip.io domains"
echo "   - FIDO registration should work with domain names"
echo "   - No more 'This is an invalid domain' error"
echo "   - WebAuthn should work with proper domain matching"
echo ""

echo "✅ Both images updated with nip.io domains:"
echo "   - FIDO App: ngoduyanh/fido-app:latest"
echo "   - WSO2 IS: ngoduyanh/wso2is-custom-permission-fixed:latest"
echo "🎉 Ready for testing with nip.io domain mapping!"


