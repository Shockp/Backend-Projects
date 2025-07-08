# Weather API Wrapper Service
https://roadmap.sh/projects/weather-api-wrapper-service

A Spring Boot microservice that wraps the Visual Crossing Weather API, providing RESTful endpoints for weather data with Redis caching and Bucket4j rate limiting.

## Tech Stack
- Java 21
- Spring Boot 3
- Jackson (JSON processing)
- Redis (caching)
- Bucket4j (rate limiting)
- Visual Crossing Weather API

## Architecture
- Hexagonal (Ports & Adapters) + Layered (3-tier)
- Clean separation of domain, application, and infrastructure

## Features
- REST API for weather data
- Integration with Visual Crossing Weather API
- Redis caching for fast responses
- Bucket4j rate limiting to prevent abuse
- OpenAPI/Swagger documentation

## Getting Started
1. Clone the repository
2. Configure `application.properties` with your Visual Crossing API key and Redis settings
3. Build and run with Maven:
   ```sh
   mvn clean spring-boot:run
   ```
4. Access API docs at `/swagger-ui.html`

## Folder Structure
- `src/main/java/com/shockp/weather/` - Main source code
- `src/main/resources/` - Configuration files
- `src/test/java/com/shockp/weather/` - Tests
- `UML Diagrams/` - Architecture diagrams