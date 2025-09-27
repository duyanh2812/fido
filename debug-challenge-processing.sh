#!/bin/bash

echo "🔍 Debug Challenge Processing Error"
echo "===================================="

echo "📋 Lỗi 'The operation is insecure' ngay sau khi lấy challenge có thể do:"
echo "   1. Challenge không được convert đúng từ base64"
echo "   2. User ID không được convert đúng từ base64"
echo "   3. rp.id không khớp với domain hiện tại"
echo "   4. HTTPS không hoạt động đúng"
echo ""

echo "🚀 Để debug chi tiết:"
echo "   1. Pull latest image: docker-compose pull"
echo "   2. Restart services: docker-compose up -d"
echo "   3. Mở browser console và tìm các debug logs sau:"
echo ""
echo "   🔍 Debug logs cần kiểm tra:"
echo "   - '🔍 Full registration options from server: [object]'"
echo "   - '🔍 Raw challenge from server: [base64 string]'"
echo "   - '🔍 Converted challenge: [ArrayBuffer]'"
echo "   - '🔍 Raw user ID from server: [base64 string]'"
echo "   - '🔍 Converted user ID: [ArrayBuffer]'"
echo "   - '🔍 rpId from server: [value or null]'"
echo "   - '🔍 Final rpId we will use: [final value]'"
echo ""

echo "🔧 Các lỗi có thể gặp:"
echo "   - Nếu 'Converted challenge' là null/undefined: Challenge conversion failed"
echo "   - Nếu 'Converted user ID' là null/undefined: User ID conversion failed"
echo "   - Nếu 'rpId from server' là null: WSO2 IS không trả về rp.id"
echo "   - Nếu 'Final rpId' không khớp với hostname: Domain mismatch"
echo ""

echo "✅ Image mới với debug logging chi tiết:"
echo "   - ngoduyanh/fido-app:latest"
echo "🎉 Sẵn sàng để debug!"

