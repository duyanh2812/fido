#!/bin/bash

echo "🚀 Deploying FIDO App to AWS"
echo "=============================="

# Check if deployment-aws.toml exists
if [ ! -f "deployment-aws.toml" ]; then
    echo "❌ deployment-aws.toml not found. Please create it first."
    exit 1
fi

echo "📋 Files to copy to AWS:"
echo "   - docker-compose.yml"
echo "   - deployment-aws.toml"
echo ""

echo "🔧 AWS Deployment Commands:"
echo "=============================="
echo ""
echo "1. Copy files to AWS instance:"
echo "   scp -i your-key.pem docker-compose.yml deployment-aws.toml ec2-user@18.143.154.126:~/"
echo ""
echo "2. SSH to AWS instance:"
echo "   ssh -i your-key.pem ec2-user@18.143.154.126"
echo ""
echo "3. Run on AWS instance:"
echo "   docker-compose pull"
echo "   docker-compose up -d"
echo "   docker-compose logs -f fido-app"
echo ""
echo "4. Test FIDO registration:"
echo "   curl -k https://18.143.154.126:8080/actuator/health"
echo ""

echo "✅ AWS Deployment Instructions Ready!"

