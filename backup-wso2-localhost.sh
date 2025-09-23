#!/bin/bash

# Script to backup WSO2 IS localhost data before Docker migration

WSO2_LOCALHOST_PATH="/Users/anhngo/Library/WSO2/wso2is-7.1.0"
BACKUP_DIR="./wso2-backup-$(date +%Y%m%d-%H%M%S)"

echo "🔄 Creating backup of WSO2 IS localhost data..."

# Create backup directory
mkdir -p "$BACKUP_DIR"

echo "📁 Backing up WSO2 IS data from: $WSO2_LOCALHOST_PATH"
echo "📁 Backup destination: $BACKUP_DIR"

# Backup key directories
echo "📦 Backing up database..."
cp -r "$WSO2_LOCALHOST_PATH/repository/database" "$BACKUP_DIR/"

echo "📦 Backing up configuration..."
cp -r "$WSO2_LOCALHOST_PATH/repository/conf" "$BACKUP_DIR/"

echo "📦 Backing up logs..."
cp -r "$WSO2_LOCALHOST_PATH/repository/logs" "$BACKUP_DIR/"

echo "📦 Backing up deployment..."
cp -r "$WSO2_LOCALHOST_PATH/repository/deployment" "$BACKUP_DIR/"

echo "📦 Backing up tenants..."
cp -r "$WSO2_LOCALHOST_PATH/repository/tenants" "$BACKUP_DIR/"

echo "📦 Backing up components..."
cp -r "$WSO2_LOCALHOST_PATH/repository/components" "$BACKUP_DIR/"

echo "📦 Backing up data..."
cp -r "$WSO2_LOCALHOST_PATH/repository/data" "$BACKUP_DIR/"

echo "📦 Backing up resources..."
cp -r "$WSO2_LOCALHOST_PATH/repository/resources" "$BACKUP_DIR/"

# Create backup info file
cat > "$BACKUP_DIR/backup-info.txt" << EOF
WSO2 IS Localhost Backup
========================
Backup Date: $(date)
Source Path: $WSO2_LOCALHOST_PATH
Backup Path: $BACKUP_DIR

Directories backed up:
- database/ (H2 database files)
- conf/ (Configuration files)
- logs/ (Log files)
- deployment/ (Deployed applications)
- tenants/ (Tenant data)
- components/ (Component data)
- data/ (Application data)
- resources/ (Resource files)

To restore:
1. Stop WSO2 IS localhost
2. Replace directories in $WSO2_LOCALHOST_PATH/repository/
3. Start WSO2 IS localhost
EOF

echo ""
echo "✅ Backup completed successfully!"
echo "📁 Backup location: $BACKUP_DIR"
echo "📄 Backup info: $BACKUP_DIR/backup-info.txt"
echo ""
echo "💡 To restore from backup:"
echo "   ./restore-wso2-backup.sh $BACKUP_DIR"
