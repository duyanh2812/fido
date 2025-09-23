#!/bin/bash

# Script to restore WSO2 IS localhost data from backup

if [ $# -eq 0 ]; then
    echo "❌ Error: Please provide backup directory path"
    echo "Usage: $0 <backup-directory>"
    echo "Example: $0 ./wso2-backup-20240923-123456"
    exit 1
fi

BACKUP_DIR="$1"
WSO2_LOCALHOST_PATH="/Users/anhngo/Library/WSO2/wso2is-7.1.0"

if [ ! -d "$BACKUP_DIR" ]; then
    echo "❌ Error: Backup directory not found: $BACKUP_DIR"
    exit 1
fi

echo "🔄 Restoring WSO2 IS localhost data from backup..."
echo "📁 Backup source: $BACKUP_DIR"
echo "📁 Target path: $WSO2_LOCALHOST_PATH"

# Check if WSO2 IS is running
if pgrep -f "wso2is" > /dev/null; then
    echo "⚠️  Warning: WSO2 IS appears to be running. Please stop it first."
    echo "   You can stop it with: pkill -f wso2is"
    read -p "Continue anyway? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "❌ Restore cancelled"
        exit 1
    fi
fi

# Create WSO2 directory if it doesn't exist
mkdir -p "$WSO2_LOCALHOST_PATH/repository"

echo "📦 Restoring database..."
cp -r "$BACKUP_DIR/database" "$WSO2_LOCALHOST_PATH/repository/"

echo "📦 Restoring configuration..."
cp -r "$BACKUP_DIR/conf" "$WSO2_LOCALHOST_PATH/repository/"

echo "📦 Restoring logs..."
cp -r "$BACKUP_DIR/logs" "$WSO2_LOCALHOST_PATH/repository/"

echo "📦 Restoring deployment..."
cp -r "$BACKUP_DIR/deployment" "$WSO2_LOCALHOST_PATH/repository/"

echo "📦 Restoring tenants..."
cp -r "$BACKUP_DIR/tenants" "$WSO2_LOCALHOST_PATH/repository/"

echo "📦 Restoring components..."
cp -r "$BACKUP_DIR/components" "$WSO2_LOCALHOST_PATH/repository/"

echo "📦 Restoring data..."
cp -r "$BACKUP_DIR/data" "$WSO2_LOCALHOST_PATH/repository/"

echo "📦 Restoring resources..."
cp -r "$BACKUP_DIR/resources" "$WSO2_LOCALHOST_PATH/repository/"

echo ""
echo "✅ Restore completed successfully!"
echo "📁 Restored to: $WSO2_LOCALHOST_PATH"
echo ""
echo "🚀 You can now start WSO2 IS localhost with all your data restored."
