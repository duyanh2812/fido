#!/bin/bash

echo "🔍 Checking Docker services status..."

# Check if services are running
echo ""
echo "📊 Service Status:"
docker-compose ps

echo ""
echo "🌐 Testing service endpoints..."

# Test FIDO App
echo "🔧 Testing FIDO Application (https://localhost:8080)..."
if curl -s -k -f https://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo "✅ FIDO App is healthy (HTTPS)"
    curl -s -k https://localhost:8080/actuator/health | jq . 2>/dev/null || curl -s -k https://localhost:8080/actuator/health
else
    echo "❌ FIDO App is not responding"
fi

echo ""
echo "🔐 Testing WSO2 IS (https://localhost:9443)..."
if curl -s -k -f https://localhost:9443/carbon/admin/login.jsp > /dev/null 2>&1; then
    echo "✅ WSO2 IS is healthy"
else
    echo "❌ WSO2 IS is not responding"
fi

echo ""
echo "📋 Summary:"
echo "- FIDO App: https://localhost:8080 (HTTPS)"
echo "- WSO2 IS: https://localhost:9443"
echo "- WSO2 IS Admin: https://localhost:9443/carbon/admin/login.jsp"
echo "- Default WSO2 credentials: admin/admin"

echo ""
echo "🔧 Useful commands:"
echo "- View logs: docker-compose logs -f"
echo "- Stop services: docker-compose down"
echo "- Restart services: docker-compose restart"
echo "- Update WSO2 config: ./update-wso2-config.sh"
