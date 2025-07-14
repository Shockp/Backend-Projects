# Weather API Wrapper Service

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.3-brightgreen.svg)](https://spring.io/projects/spring-boot)

> **Project Reference**: [roadmap.sh/projects/weather-api-wrapper-service](https://roadmap.sh/projects/weather-api-wrapper-service)

A production-ready Spring Boot microservice that wraps the Visual Crossing Weather API, providing secure RESTful endpoints for weather data with comprehensive caching, rate limiting, and monitoring capabilities.

## 🚀 Features

### Core Functionality
- **RESTful Weather API**: Clean endpoints for weather data retrieval
- **Visual Crossing Integration**: Seamless integration with Visual Crossing Weather API
- **Redis Caching**: High-performance caching for improved response times
- **Rate Limiting**: Bucket4j-based protection against API abuse
- **Comprehensive Validation**: Input validation and sanitization at all levels

### Production-Ready Features
- **Security Hardening**: OWASP-compliant security configurations
- **Graceful Shutdown**: Proper resource cleanup and shutdown handling
- **Comprehensive Logging**: Structured logging with performance monitoring
- **Error Handling**: Robust error handling with proper HTTP status codes
- **Health Monitoring**: Application health checks and performance metrics

## 🏗️ Architecture

The service follows **Hexagonal Architecture (Ports & Adapters)** with **Clean Architecture** principles:

```
┌─────────────────────────────────────────┐
│           Infrastructure Layer          │
│  ┌─────────────┐  ┌─────────────────┐   │
│  │ Web Layer   │  │ External APIs   │   │
│  │ (REST API)  │  │ (Redis, Visual  │   │
│  │             │  │  Crossing)      │   │
│  └─────────────┘  └─────────────────┘   │
└─────────────────────────────────────────┘
┌─────────────────────────────────────────┐
│           Application Layer             │
│  ┌─────────────┐  ┌─────────────────┐   │
│  │ Use Cases   │  │ Ports           │   │
│  │             │  │ (Interfaces)    │   │
│  └─────────────┘  └─────────────────┘   │
└─────────────────────────────────────────┘
┌─────────────────────────────────────────┐
│              Domain Layer               │
│  ┌─────────────┐  ┌─────────────────┐   │
│  │ Models      │  │ Services        │   │
│  │             │  │                 │   │
│  └─────────────┘  └─────────────────┘   │
└─────────────────────────────────────────┘
```

### Layer Responsibilities
- **Domain Layer**: Core business logic and entities (no external dependencies)
- **Application Layer**: Use cases and port definitions (depends only on domain)
- **Infrastructure Layer**: External integrations and adapters (implements ports)

## 🛠️ Tech Stack

### Core Technologies
- **Java 21**: Latest LTS version with modern language features
- **Spring Boot 3.5.3**: Latest Spring Boot with enhanced security and performance
- **Spring WebFlux**: Reactive web stack for external API calls
- **Jackson**: JSON processing and serialization

### External Dependencies
- **Redis**: High-performance caching and session storage
- **Bucket4j 8.7.0**: Advanced rate limiting with token bucket algorithm
- **Visual Crossing Weather API**: External weather data provider

### Development & Testing
- **Maven**: Dependency management and build automation
- **JUnit 5**: Unit and integration testing
- **Mockito**: Mocking framework for testing
- **Spring Boot Test**: Testing utilities and test slices

## 📁 Project Structure

```
weather-api-wrapper-service/
├── src/main/java/com/shockp/weather/
│   ├── WeatherApiWrapperApplication.java      # Main application class
│   ├── domain/                                # Domain layer
│   │   ├── model/                            # Domain entities
│   │   │   ├── Location.java
│   │   │   ├── WeatherData.java
│   │   │   ├── WeatherRequest.java
│   │   │   └── WeatherResponse.java
│   │   └── service/                          # Domain services
│   │       ├── WeatherService.java
│   │       ├── CacheService.java
│   │       └── RateLimiterService.java
│   ├── application/                          # Application layer
│   │   ├── port/                            # Port interfaces
│   │   │   ├── WeatherProviderPort.java
│   │   │   ├── CachePort.java
│   │   │   └── RateLimiterPort.java
│   │   └── usecase/                         # Use cases
│   │       ├── weather/
│   │       ├── cache/
│   │       └── ratelimit/
│   └── infrastructure/                       # Infrastructure layer
│       ├── cache/                           # Cache adapters
│       ├── config/                          # Configuration
│       ├── provider/                        # External API adapters
│       ├── ratelimiter/                     # Rate limiting adapters
│       └── web/                             # REST controllers
├── src/main/resources/
│   └── application.properties               # Configuration file
├── src/test/java/                          # Test sources
└── UML Diagrams/                           # Architecture documentation
    ├── ClassDiagram.md
    ├── ComponentDiagram.md
    ├── PackageDiagram.md
    ├── SequenceDiagram.md
    └── UseCaseDiagram.md
```

## 🚀 Getting Started

### Prerequisites
- **Java 21** or later
- **Maven 3.6+**
- **Redis Server** (local or remote)
- **Visual Crossing Weather API Key**

### Installation & Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd weather-api-wrapper-service
   ```

2. **Configure application properties**
   ```bash
   cp src/main/resources/application.properties.template src/main/resources/application.properties
   ```

3. **Update configuration in `application.properties`**
   ```properties
   # Visual Crossing Weather API
   weather.visualcrossing.api-key=YOUR_API_KEY_HERE
   weather.visualcrossing.base-url=https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline
   
   # Redis configuration
   spring.data.redis.host=localhost
   spring.data.redis.port=6379
   
   # Rate limiting
   rate-limiter.capacity=100
   rate-limiter.refill=60
   
   # Server configuration
   server.port=8080
   ```

4. **Start Redis (if running locally)**
   ```bash
   # Using Docker
   docker run -d -p 6379:6379 redis:7-alpine
   
   # Or using local installation
   redis-server
   ```

5. **Build and run the application**
   ```bash
   # Build the project
   mvn clean compile
   
   # Run tests
   mvn test
   
   # Run the application
   mvn spring-boot:run
   ```

6. **Verify the application is running**
   ```bash
   curl http://localhost:8080/actuator/health
   ```

## 📡 API Endpoints

### Weather Data Endpoints

#### Get Weather by Location
```http
GET /api/weather?location={location}&date={date}&includeHourly={boolean}
```

**Parameters:**
- `location` (required): City name or "latitude,longitude"
- `date` (optional): Date in YYYY-MM-DD format (defaults to today)
- `includeHourly` (optional): Include hourly data (default: false)

**Example:**
```bash
curl "http://localhost:8080/api/weather?location=London&date=2024-01-15"
```

#### Get Weather by Coordinates
```http
GET /api/weather/coordinates?lat={latitude}&lon={longitude}&date={date}
```

**Example:**
```bash
curl "http://localhost:8080/api/weather/coordinates?lat=51.5074&lon=-0.1278"
```

### Cache Management Endpoints

#### Get Cache Status
```http
GET /api/cache/status/{key}
```

#### Clear Cache
```http
DELETE /api/cache/{key}
```

### Rate Limiting Endpoints

#### Get Rate Limit Status
```http
GET /api/rate-limit/status/{clientId}
```

#### Reset Rate Limit
```http
POST /api/rate-limit/reset/{clientId}
```

### Response Format

```json
{
  "weatherData": {
    "temperature": 15.5,
    "humidity": 65,
    "description": "Partly cloudy",
    "timestamp": "2024-01-15T10:30:00",
    "location": {
      "latitude": 51.5074,
      "longitude": -0.1278,
      "city": "London",
      "country": "UK"
    }
  },
  "cached": true,
  "timestamp": "2024-01-15T10:30:00"
}
```

## 🔧 Configuration

### Application Properties

| Property | Description | Default |
|----------|-------------|---------|
| `weather.visualcrossing.api-key` | Visual Crossing API key | Required |
| `weather.visualcrossing.base-url` | API base URL | Visual Crossing URL |
| `spring.data.redis.host` | Redis host | localhost |
| `spring.data.redis.port` | Redis port | 6379 |
| `rate-limiter.capacity` | Rate limit capacity | 100 |
| `rate-limiter.refill` | Rate limit refill rate | 60 |
| `weather.cache.timeout` | Cache TTL | PT30M (30 minutes) |
| `server.port` | Server port | 8080 |

### Security Configuration

The application automatically configures security features:
- Secure cookie settings
- Security headers
- JMX disabled
- Actuator endpoints secured

## 🧪 Testing

### Run Tests
```bash
# Run all tests
mvn test

# Run tests with coverage
mvn test jacoco:report

# Run integration tests only
mvn test -Dtest="*IT"
```

### Test Categories
- **Unit Tests**: Domain logic and individual components
- **Integration Tests**: API endpoints and external integrations
- **Cache Tests**: Redis caching functionality
- **Rate Limiting Tests**: Bucket4j rate limiting

## 🚀 Production Deployment

### Docker Deployment

1. **Build Docker image**
   ```bash
   mvn spring-boot:build-image
   ```

2. **Run with Docker Compose**
   ```yaml
   version: '3.8'
   services:
     weather-api:
       image: weather-api-wrapper-service:latest
       ports:
         - "8080:8080"
       environment:
         - WEATHER_API_KEY=your_api_key
         - REDIS_HOST=redis
       depends_on:
         - redis
     
     redis:
       image: redis:7-alpine
       ports:
         - "6379:6379"
   ```

### Environment Variables

For production deployment, use environment variables:
```bash
export WEATHER_VISUALCROSSING_API_KEY=your_api_key
export SPRING_DATA_REDIS_HOST=redis.example.com
export SPRING_DATA_REDIS_PASSWORD=redis_password
export SERVER_PORT=8080
```

## 📊 Monitoring & Observability

### Health Checks
- **Application Health**: `/actuator/health`
- **Redis Health**: Automatic health indicator
- **Custom Health Checks**: Weather provider availability

### Metrics
- Request/response metrics
- Cache hit/miss ratios
- Rate limiting statistics
- External API response times

### Logging
- Structured JSON logging
- Request/response logging
- Performance monitoring
- Error tracking with stack traces

## 🔒 Security Features

### Input Validation
- Location and coordinate validation
- Date format validation
- Parameter sanitization
- SQL injection prevention

### Rate Limiting
- Per-client rate limiting
- Token bucket algorithm
- Configurable limits
- Graceful degradation

### Security Headers
- HTTPS enforcement
- Secure cookie settings
- XSS protection
- CSRF protection

## 🤝 Contributing

### Development Guidelines
1. Follow Clean Architecture principles
2. Write comprehensive tests
3. Update documentation
4. Follow Google Java Style Guide
5. Add appropriate logging

### Code Quality
- Minimum 80% test coverage
- No compiler warnings
- Pass all static analysis checks
- Follow security best practices

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🔗 Links

- [Visual Crossing Weather API](https://www.visualcrossing.com/weather-api)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Redis Documentation](https://redis.io/documentation)
- [Bucket4j Documentation](https://bucket4j.com/)

## 📞 Support

For questions, issues, or contributions:
1. Check existing [GitHub Issues](../../issues)
2. Create a new issue with detailed description
3. Follow the issue template guidelines
