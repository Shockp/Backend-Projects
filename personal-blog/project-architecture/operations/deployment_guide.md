# Personal Blog - Deployment Guide

## Overview

This guide covers the deployment of the Personal Blog application using modern containerization and cloud deployment strategies. The application is designed to be deployed on various platforms including Railway, Render, or traditional cloud providers.

## Prerequisites

### Required Tools
- Docker Desktop 4.20+
- Docker Compose 2.20+
- Git
- Maven 3.9+
- Java 21+

### Required Accounts
- GitHub account (for source code)
- Railway/Render account (for hosting)
- PostgreSQL database service (or use platform-provided database)

## Environment Configuration

### Environment Variables

Create environment-specific configuration files:

#### Production Environment Variables
```bash
# Database Configuration
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/personal_blog_prod
SPRING_DATASOURCE_USERNAME=blog_user
SPRING_DATASOURCE_PASSWORD=secure_password_here

# JWT Configuration
JWT_SECRET=your-super-secure-jwt-secret-key-here-minimum-256-bits
JWT_EXPIRATION=3600000
JWT_REFRESH_EXPIRATION=86400000

# Application Configuration
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8080
APP_BASE_URL=https://yourdomain.com

# Security Configuration
CORS_ALLOWED_ORIGINS=https://yourdomain.com,https://www.yourdomain.com
SECURE_COOKIES=true

# Logging Configuration
LOGGING_LEVEL_ROOT=INFO
LOGGING_LEVEL_COM_PERSONALBLOG=INFO

# Cache Configuration
SPRING_CACHE_TYPE=caffeine
CACHE_POSTS_TTL=3600
CACHE_CATEGORIES_TTL=7200

# File Upload Configuration
FILE_UPLOAD_MAX_SIZE=10MB
FILE_UPLOAD_PATH=/app/uploads

# Email Configuration (if needed)
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=your-email@gmail.com
SPRING_MAIL_PASSWORD=your-app-password
```

#### Development Environment Variables
```bash
# Database Configuration
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/personal_blog_dev
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres

# JWT Configuration
JWT_SECRET=dev-jwt-secret-key-for-development-only
JWT_EXPIRATION=3600000
JWT_REFRESH_EXPIRATION=86400000

# Application Configuration
SPRING_PROFILES_ACTIVE=dev
SERVER_PORT=8080
APP_BASE_URL=http://localhost:8080

# Security Configuration
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:8080
SECURE_COOKIES=false

# Logging Configuration
LOGGING_LEVEL_ROOT=INFO
LOGGING_LEVEL_COM_PERSONALBLOG=DEBUG

# Development Tools
SPRING_DEVTOOLS_RESTART_ENABLED=true
SPRING_H2_CONSOLE_ENABLED=false
```

## Docker Configuration

### Dockerfile

Create a multi-stage Dockerfile for optimal production builds:

```dockerfile
# Build stage
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build application
RUN ./mvnw clean package -DskipTests -B

# Runtime stage
FROM eclipse-temurin:21-jre-alpine AS runtime

# Create non-root user
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Install required packages
RUN apk add --no-cache \
    curl \
    tzdata

# Set timezone
ENV TZ=UTC

WORKDIR /app

# Copy JAR from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Create uploads directory
RUN mkdir -p /app/uploads && \
    chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# JVM optimization for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -XX:+UseStringDeduplication"

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

### Docker Compose

#### Development Environment

Create `docker-compose.dev.yml`:

```yaml
version: '3.8'

services:
  app:
    build:
      context: .
      dockerfile: Dockerfile
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=dev
      - SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/personal_blog_dev
      - SPRING_DATASOURCE_USERNAME=postgres
      - SPRING_DATASOURCE_PASSWORD=postgres
      - JWT_SECRET=dev-jwt-secret-key-for-development-only
    depends_on:
      db:
        condition: service_healthy
    volumes:
      - ./uploads:/app/uploads
    networks:
      - blog-network

  db:
    image: postgres:15-alpine
    environment:
      - POSTGRES_DB=personal_blog_dev
      - POSTGRES_USER=postgres
      - POSTGRES_PASSWORD=postgres
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./database/init.sql:/docker-entrypoint-initdb.d/init.sql
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 10s
      timeout: 5s
      retries: 5
    networks:
      - blog-network

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 3s
      retries: 5
    networks:
      - blog-network

volumes:
  postgres_data:
  redis_data:

networks:
  blog-network:
    driver: bridge
```

#### Production Environment

Create `docker-compose.prod.yml`:

```yaml
version: '3.8'

services:
  app:
    build:
      context: .
      dockerfile: Dockerfile
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
    env_file:
      - .env.prod
    restart: unless-stopped
    volumes:
      - ./uploads:/app/uploads
      - ./logs:/app/logs
    networks:
      - blog-network
    depends_on:
      db:
        condition: service_healthy

  db:
    image: postgres:15-alpine
    environment:
      - POSTGRES_DB=${POSTGRES_DB}
      - POSTGRES_USER=${POSTGRES_USER}
      - POSTGRES_PASSWORD=${POSTGRES_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./database/backup:/backup
    restart: unless-stopped
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${POSTGRES_USER}"]
      interval: 30s
      timeout: 10s
      retries: 3
    networks:
      - blog-network

  nginx:
    image: nginx:alpine
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx/nginx.conf:/etc/nginx/nginx.conf
      - ./nginx/ssl:/etc/nginx/ssl
      - ./uploads:/var/www/uploads
    restart: unless-stopped
    depends_on:
      - app
    networks:
      - blog-network

volumes:
  postgres_data:

networks:
  blog-network:
    driver: bridge
```

## Platform-Specific Deployment

### Railway Deployment

#### 1. Prepare Railway Configuration

Create `railway.json`:

```json
{
  "$schema": "https://railway.app/railway.schema.json",
  "build": {
    "builder": "DOCKERFILE",
    "dockerfilePath": "Dockerfile"
  },
  "deploy": {
    "numReplicas": 1,
    "sleepApplication": false,
    "restartPolicyType": "ON_FAILURE"
  }
}
```

#### 2. Environment Variables Setup

In Railway dashboard, set these environment variables:

```bash
# Database (use Railway PostgreSQL addon)
DATABASE_URL=postgresql://username:password@host:port/database
SPRING_DATASOURCE_URL=${DATABASE_URL}

# Application
SPRING_PROFILES_ACTIVE=prod
PORT=8080
JWT_SECRET=your-production-jwt-secret

# Domain
RAILWAY_STATIC_URL=your-app.railway.app
APP_BASE_URL=https://${RAILWAY_STATIC_URL}
```

#### 3. Deploy Steps

```bash
# Install Railway CLI
npm install -g @railway/cli

# Login to Railway
railway login

# Initialize project
railway init

# Add PostgreSQL service
railway add postgresql

# Deploy
railway up
```

### Render Deployment

#### 1. Create `render.yaml`

```yaml
services:
  - type: web
    name: personal-blog
    env: docker
    dockerfilePath: ./Dockerfile
    plan: starter
    region: oregon
    branch: main
    healthCheckPath: /actuator/health
    envVars:
      - key: SPRING_PROFILES_ACTIVE
        value: prod
      - key: JWT_SECRET
        generateValue: true
      - key: DATABASE_URL
        fromDatabase:
          name: personal-blog-db
          property: connectionString
      - key: SPRING_DATASOURCE_URL
        fromDatabase:
          name: personal-blog-db
          property: connectionString

databases:
  - name: personal-blog-db
    plan: starter
    databaseName: personal_blog
    user: blog_user
```

#### 2. Deploy Steps

1. Connect GitHub repository to Render
2. Create new Web Service
3. Select Docker environment
4. Add PostgreSQL database
5. Configure environment variables
6. Deploy

### Traditional Cloud Deployment (AWS/GCP/Azure)

#### AWS ECS Deployment

1. **Create ECR Repository**

```bash
# Create repository
aws ecr create-repository --repository-name personal-blog

# Get login token
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin <account-id>.dkr.ecr.us-east-1.amazonaws.com

# Build and push image
docker build -t personal-blog .
docker tag personal-blog:latest <account-id>.dkr.ecr.us-east-1.amazonaws.com/personal-blog:latest
docker push <account-id>.dkr.ecr.us-east-1.amazonaws.com/personal-blog:latest
```

2. **Create ECS Task Definition**

```json
{
  "family": "personal-blog",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "512",
  "memory": "1024",
  "executionRoleArn": "arn:aws:iam::account:role/ecsTaskExecutionRole",
  "containerDefinitions": [
    {
      "name": "personal-blog",
      "image": "<account-id>.dkr.ecr.us-east-1.amazonaws.com/personal-blog:latest",
      "portMappings": [
        {
          "containerPort": 8080,
          "protocol": "tcp"
        }
      ],
      "environment": [
        {
          "name": "SPRING_PROFILES_ACTIVE",
          "value": "prod"
        }
      ],
      "secrets": [
        {
          "name": "SPRING_DATASOURCE_URL",
          "valueFrom": "arn:aws:secretsmanager:us-east-1:account:secret:blog/database-url"
        }
      ],
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "/ecs/personal-blog",
          "awslogs-region": "us-east-1",
          "awslogs-stream-prefix": "ecs"
        }
      }
    }
  ]
}
```

## Database Migration

### Production Database Setup

1. **Create Production Database**

```sql
-- Connect as superuser
CREATE DATABASE personal_blog_prod;
CREATE USER blog_user WITH ENCRYPTED PASSWORD 'secure_password';
GRANT ALL PRIVILEGES ON DATABASE personal_blog_prod TO blog_user;

-- Connect to personal_blog_prod database
GRANT ALL ON SCHEMA public TO blog_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO blog_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO blog_user;
```

2. **Run Flyway Migrations**

```bash
# Using Maven
mvn flyway:migrate -Dflyway.url=jdbc:postgresql://prod-host:5432/personal_blog_prod -Dflyway.user=blog_user -Dflyway.password=secure_password

# Using Docker
docker run --rm \
  -v $(pwd)/src/main/resources/db/migration:/flyway/sql \
  flyway/flyway:latest \
  -url=jdbc:postgresql://prod-host:5432/personal_blog_prod \
  -user=blog_user \
  -password=secure_password \
  migrate
```

### Database Backup Strategy

#### Automated Backup Script

Create `scripts/backup-db.sh`:

```bash
#!/bin/bash

# Configuration
DB_HOST="your-db-host"
DB_NAME="personal_blog_prod"
DB_USER="blog_user"
BACKUP_DIR="/backup/postgresql"
DATE=$(date +"%Y%m%d_%H%M%S")
BACKUP_FILE="${BACKUP_DIR}/personal_blog_${DATE}.sql"

# Create backup directory
mkdir -p $BACKUP_DIR

# Create backup
pg_dump -h $DB_HOST -U $DB_USER -d $DB_NAME > $BACKUP_FILE

# Compress backup
gzip $BACKUP_FILE

# Remove backups older than 30 days
find $BACKUP_DIR -name "personal_blog_*.sql.gz" -mtime +30 -delete

echo "Backup completed: ${BACKUP_FILE}.gz"
```

#### Cron Job Setup

```bash
# Add to crontab
0 2 * * * /path/to/scripts/backup-db.sh
```

## Monitoring and Logging

### Application Monitoring

#### Health Checks

The application includes Spring Boot Actuator endpoints:

- `/actuator/health` - Application health status
- `/actuator/metrics` - Application metrics
- `/actuator/info` - Application information
- `/actuator/prometheus` - Prometheus metrics

#### Logging Configuration

Create `logback-spring.xml` for structured logging:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <springProfile name="prod">
        <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
            <encoder class="net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder">
                <providers>
                    <timestamp/>
                    <logLevel/>
                    <loggerName/>
                    <message/>
                    <mdc/>
                    <stackTrace/>
                </providers>
            </encoder>
        </appender>
        
        <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
            <file>/app/logs/application.log</file>
            <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
                <fileNamePattern>/app/logs/application.%d{yyyy-MM-dd}.%i.log</fileNamePattern>
                <maxFileSize>100MB</maxFileSize>
                <maxHistory>30</maxHistory>
                <totalSizeCap>3GB</totalSizeCap>
            </rollingPolicy>
            <encoder class="net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder">
                <providers>
                    <timestamp/>
                    <logLevel/>
                    <loggerName/>
                    <message/>
                    <mdc/>
                    <stackTrace/>
                </providers>
            </encoder>
        </appender>
        
        <root level="INFO">
            <appender-ref ref="STDOUT"/>
            <appender-ref ref="FILE"/>
        </root>
    </springProfile>
</configuration>
```

### External Monitoring Services

#### New Relic Integration

```bash
# Add to Dockerfile
RUN curl -O https://download.newrelic.com/newrelic/java-agent/newrelic-agent/current/newrelic-java.zip && \
    unzip newrelic-java.zip && \
    mv newrelic /app/

# Update ENTRYPOINT
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -javaagent:/app/newrelic/newrelic.jar -jar app.jar"]
```

#### Datadog Integration

```bash
# Add to environment variables
DD_API_KEY=your-datadog-api-key
DD_SITE=datadoghq.com
DD_SERVICE=personal-blog
DD_ENV=production
DD_VERSION=1.0.0
```

## Security Considerations

### SSL/TLS Configuration

#### Nginx SSL Configuration

Create `nginx/nginx.conf`:

```nginx
server {
    listen 80;
    server_name yourdomain.com www.yourdomain.com;
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name yourdomain.com www.yourdomain.com;

    ssl_certificate /etc/nginx/ssl/cert.pem;
    ssl_certificate_key /etc/nginx/ssl/key.pem;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers ECDHE-RSA-AES256-GCM-SHA512:DHE-RSA-AES256-GCM-SHA512:ECDHE-RSA-AES256-GCM-SHA384:DHE-RSA-AES256-GCM-SHA384;
    ssl_prefer_server_ciphers off;
    ssl_session_cache shared:SSL:10m;
    ssl_session_timeout 10m;

    # Security headers
    add_header X-Frame-Options DENY;
    add_header X-Content-Type-Options nosniff;
    add_header X-XSS-Protection "1; mode=block";
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;

    location / {
        proxy_pass http://app:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location /uploads/ {
        alias /var/www/uploads/;
        expires 1y;
        add_header Cache-Control "public, immutable";
    }
}
```

### Environment Security

1. **Never commit secrets to version control**
2. **Use environment variables for all sensitive data**
3. **Rotate JWT secrets regularly**
4. **Use strong database passwords**
5. **Enable database SSL connections**
6. **Implement proper CORS policies**
7. **Use HTTPS in production**
8. **Regular security updates**

## Troubleshooting

### Common Issues

#### Application Won't Start

```bash
# Check logs
docker logs <container-id>

# Check environment variables
docker exec <container-id> env

# Check database connectivity
docker exec <container-id> pg_isready -h db -p 5432
```

#### Database Connection Issues

```bash
# Test database connection
psql -h localhost -p 5432 -U blog_user -d personal_blog_prod

# Check database logs
docker logs <postgres-container-id>
```

#### Performance Issues

```bash
# Check application metrics
curl http://localhost:8080/actuator/metrics

# Monitor resource usage
docker stats

# Check database performance
SELECT * FROM pg_stat_activity;
```

### Rollback Strategy

#### Application Rollback

```bash
# Railway
railway rollback

# Render
# Use Render dashboard to rollback to previous deployment

# Docker
docker pull <previous-image-tag>
docker stop <current-container>
docker run <previous-image-tag>
```

#### Database Rollback

```bash
# Restore from backup
psql -h localhost -U blog_user -d personal_blog_prod < backup_file.sql

# Flyway rollback (if using Flyway Teams)
mvn flyway:undo
```

This deployment guide provides comprehensive instructions for deploying the Personal Blog application across various platforms while maintaining security, performance, and reliability standards.