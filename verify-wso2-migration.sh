#!/bin/bash

# Script to verify WSO2 IS data migration from localhost to Docker

WSO2_LOCALHOST_PATH="/Users/anhngo/Library/WSO2/wso2is-7.1.0"
DOCKER_CONTAINER="wso2is"

echo "🔍 Verifying WSO2 IS data migration from localhost to Docker..."

# Check if Docker container is running
if ! docker ps | grep -q "$DOCKER_CONTAINER"; then
    echo "❌ Error: WSO2 IS Docker container is not running"
    echo "   Start it with: docker-compose up -d"
    exit 1
fi

echo "✅ WSO2 IS Docker container is running"

# Function to compare directories
compare_directories() {
    local local_path="$1"
    local container_path="$2"
    local description="$3"
    
    echo "🔍 Checking $description..."
    
    if [ -d "$local_path" ]; then
        local_count=$(find "$local_path" -type f | wc -l)
        echo "   📁 Localhost files: $local_count"
        
        # Check if files exist in container
        container_count=$(docker exec "$DOCKER_CONTAINER" find "$container_path" -type f 2>/dev/null | wc -l)
        echo "   🐳 Container files: $container_count"
        
        if [ "$local_count" -eq "$container_count" ]; then
            echo "   ✅ File counts match"
        else
            echo "   ⚠️  File counts differ"
        fi
    else
        echo "   ❌ Local path not found: $local_path"
    fi
}

# Compare key directories
compare_directories "$WSO2_LOCALHOST_PATH/repository/database" "/home/wso2carbon/wso2is-7.1.0/repository/database" "Database"
compare_directories "$WSO2_LOCALHOST_PATH/repository/conf" "/home/wso2carbon/wso2is-7.1.0/repository/conf" "Configuration"
compare_directories "$WSO2_LOCALHOST_PATH/repository/deployment" "/home/wso2carbon/wso2is-7.1.0/repository/deployment" "Deployment"
compare_directories "$WSO2_LOCALHOST_PATH/repository/tenants" "/home/wso2carbon/wso2is-7.1.0/repository/tenants" "Tenants"

# Check specific important files
echo ""
echo "🔍 Checking important configuration files..."

important_files=(
    "deployment.toml"
    "carbon.xml"
    "user-mgt.xml"
    "registry.xml"
)

for file in "${important_files[@]}"; do
    local_file="$WSO2_LOCALHOST_PATH/repository/conf/$file"
    container_file="/home/wso2carbon/wso2is-7.1.0/repository/conf/$file"
    
    if [ -f "$local_file" ]; then
        echo "   📄 Checking $file..."
        
        # Compare file sizes
        local_size=$(stat -f%z "$local_file" 2>/dev/null || echo "0")
        container_size=$(docker exec "$DOCKER_CONTAINER" stat -c%s "$container_file" 2>/dev/null || echo "0")
        
        if [ "$local_size" -eq "$container_size" ] && [ "$local_size" -gt 0 ]; then
            echo "   ✅ $file sizes match ($local_size bytes)"
        else
            echo "   ⚠️  $file sizes differ (local: $local_size, container: $container_size)"
        fi
    fi
done

# Check database files
echo ""
echo "🔍 Checking database files..."

db_files=(
    "WSO2IDENTITY_DB.mv.db"
    "WSO2SHARED_DB.mv.db"
    "WSO2CARBON_DB.mv.db"
)

for db_file in "${db_files[@]}"; do
    local_file="$WSO2_LOCALHOST_PATH/repository/database/$db_file"
    container_file="/home/wso2carbon/wso2is-7.1.0/repository/database/$db_file"
    
    if [ -f "$local_file" ]; then
        echo "   🗄️  Checking $db_file..."
        
        local_size=$(stat -f%z "$local_file" 2>/dev/null || echo "0")
        container_size=$(docker exec "$DOCKER_CONTAINER" stat -c%s "$container_file" 2>/dev/null || echo "0")
        
        if [ "$local_size" -eq "$container_size" ] && [ "$local_size" -gt 0 ]; then
            echo "   ✅ $db_file sizes match ($local_size bytes)"
        else
            echo "   ⚠️  $db_file sizes differ (local: $local_size, container: $container_size)"
        fi
    fi
done

echo ""
echo "📋 Migration verification completed!"
echo ""
echo "🌐 Access WSO2 IS Docker:"
echo "   - Admin Console: https://localhost:9443/carbon/admin/login.jsp"
echo "   - Credentials: admin/admin"
echo ""
echo "🔧 Useful commands:"
echo "   - View logs: docker-compose logs wso2is"
echo "   - Access container: docker exec -it wso2is bash"
echo "   - Stop services: docker-compose down"
