# Docker Setup for FIDO Application with WSO2 IS 7.1.0

This document provides instructions for running the FIDO application alongside WSO2 Identity Server 7.1.0 using Docker Compose.

## Prerequisites

- Docker Desktop installed and running
- Docker Compose installed
- At least 4GB of available RAM
- Ports 8080 and 9443 available on your system

## Quick Start

### Option 1: Using the startup script
```bash
./start-docker.sh
```

### Option 2: Manual Docker Compose commands
```bash
# Build and start all services
docker-compose up --build -d

# View logs
docker-compose logs -f

# Stop services
docker-compose down
```

## Services

### WSO2 Identity Server 7.1.0
- **URL**: https://localhost:9443
- **Admin Console**: https://localhost:9443/carbon/admin/login.jsp
- **Default Credentials**: admin/admin
- **Container Name**: wso2is

### FIDO Application
- **URL**: http://localhost:8080
- **Health Check**: http://localhost:8080/actuator/health
- **Container Name**: fido-app

## Configuration

The application uses different configurations for Docker vs local development:

- **Local Development**: `application.properties` (with SSL enabled)
- **Docker Environment**: `application-docker.properties` (SSL disabled for container communication)

### WSO2 IS Configuration

The Docker setup automatically mounts your local WSO2 IS configuration file:
- **Source**: `/Users/anhngo/Library/WSO2/wso2is-7.1.0/repository/conf/deployment.toml`
- **Target**: `/home/wso2carbon/wso2is-7.1.0/repository/conf/deployment.toml` (read-only)

This includes your FIDO and CORS configurations:
- FIDO WebAuthn settings with trusted origins
- CORS configuration for localhost:8080 and localhost:9443
- Authentication framework settings

To update WSO2 configuration:
1. Edit your local deployment.toml file
2. Run: `./update-wso2-config.sh`
3. Restart services: `docker-compose restart wso2is`

## Docker Compose Services

### wso2is
- Image: `wso2/wso2is:7.1.0`
- Ports: 9443 (HTTPS), 9763 (HTTP)
- Health check: Checks WSO2 IS admin console availability
- Data persistence: Uses Docker volume `wso2is-data`

### fido-app
- Built from local Dockerfile
- Port: 8080
- Depends on WSO2 IS being healthy
- Health check: Checks Spring Boot actuator endpoint

## Useful Commands

```bash
# View service status
docker-compose ps

# View logs for specific service
docker-compose logs wso2is
docker-compose logs fido-app

# Restart a specific service
docker-compose restart fido-app

# Stop all services
docker-compose down

# Stop and remove volumes (WARNING: This will delete WSO2 IS data)
docker-compose down -v

# Rebuild and start
docker-compose up --build --force-recreate
```

## Troubleshooting

### WSO2 IS not starting
- Check if port 9443 is available
- Ensure sufficient memory (at least 2GB for WSO2 IS)
- Check logs: `docker-compose logs wso2is`

### FIDO Application not starting
- Ensure WSO2 IS is healthy first
- Check logs: `docker-compose logs fido-app`
- Verify network connectivity between containers

### Port conflicts
- Stop any services using ports 8080 or 9443
- Use `lsof -i :8080` and `lsof -i :9443` to check port usage

## Network Configuration

The services communicate through a custom Docker network `fido-network`:
- WSO2 IS internal hostname: `wso2is`
- FIDO App internal hostname: `fido-app`

## Data Persistence

WSO2 IS data is persisted in the Docker volume `wso2is-data`. This includes:
- User stores
- Service providers
- OAuth applications
- FIDO configurations

To reset WSO2 IS data:
```bash
docker-compose down -v
docker-compose up -d
```
