#!/bin/bash

# Script to check WSO2 IS configuration

WSO2_CONFIG_PATH="/Users/anhngo/Library/WSO2/wso2is-7.1.0/repository/conf/deployment.toml"

echo "🔍 Checking WSO2 IS configuration..."

# Check if the config file exists
if [ ! -f "$WSO2_CONFIG_PATH" ]; then
    echo "❌ Error: WSO2 config file not found at: $WSO2_CONFIG_PATH"
    exit 1
fi

echo "✅ WSO2 config file found at: $WSO2_CONFIG_PATH"
echo ""

# Check for FIDO configurations
echo "📋 Current FIDO and CORS configurations:"
echo ""

if grep -q "\[fido.webAuthn\]" "$WSO2_CONFIG_PATH"; then
    echo "🔐 FIDO WebAuthn configuration:"
    grep -A 5 "\[fido.webAuthn\]" "$WSO2_CONFIG_PATH" | grep -v "^$"
    echo ""
fi

if grep -q "\[cors\]" "$WSO2_CONFIG_PATH"; then
    echo "🌐 CORS configuration:"
    grep -A 10 "\[cors\]" "$WSO2_CONFIG_PATH" | grep -v "^$"
    echo ""
fi

if grep -q "\[authentication.fido2\]" "$WSO2_CONFIG_PATH"; then
    echo "🔑 FIDO2 Authentication configuration:"
    grep -A 5 "\[authentication.fido2\]" "$WSO2_CONFIG_PATH" | grep -v "^$"
    echo ""
fi

if grep -q "\[fido.trusted\]" "$WSO2_CONFIG_PATH"; then
    echo "🛡️  FIDO Trusted Origins:"
    grep -A 3 "\[fido.trusted\]" "$WSO2_CONFIG_PATH" | grep -v "^$"
    echo ""
fi

echo "📊 Configuration Summary:"
echo "- File size: $(wc -c < "$WSO2_CONFIG_PATH") bytes"
echo "- Total lines: $(wc -l < "$WSO2_CONFIG_PATH")"
echo "- Last modified: $(stat -f "%Sm" "$WSO2_CONFIG_PATH")"

echo ""
echo "🚀 To apply this configuration to Docker:"
echo "   ./update-wso2-config.sh"
