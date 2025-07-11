# Package Diagram - Weather API Wrapper Service

## Overview
This diagram shows the package structure and dependencies in the Weather API Wrapper Service, following hexagonal architecture principles.

## Package Diagram

```mermaid
graph TB
    %% Main Package
    subgraph "com.shockp.weather"
        subgraph "domain"
            subgraph "model"
                LOCATION[Location.java]
                WEATHER_DATA[WeatherData.java]
                WEATHER_REQUEST[WeatherRequest.java]
                WEATHER_RESPONSE[WeatherResponse.java]
            end
            
            subgraph "service"
                WEATHER_SERVICE[WeatherService.java]
                CACHE_SERVICE[CacheService.java]
                RATE_LIMITER_SERVICE[RateLimiterService.java]
            end
        end
        
        subgraph "application"
            subgraph "port"
                WEATHER_PROVIDER_PORT[WeatherProviderPort.java]
                CACHE_PORT[CachePort.java]
                RATE_LIMITER_PORT[RateLimiterPort.java]
            end
            
            subgraph "usecase"
                GET_WEATHER_UC[GetWeatherUseCase.java]
                CACHE_WEATHER_UC[CacheWeatherUseCase.java]
                RATE_LIMIT_UC[RateLimitUseCase.java]
            end
        end
        
        subgraph "infrastructure"
            subgraph "cache"
                REDIS_ADAPTER[RedisCacheAdapter.java]
            end
            
            subgraph "config"
                APP_CONFIG[AppConfig.java]
            end
            
            subgraph "provider"
                VC_PROVIDER[VisualCrossingWeatherProvider.java]
            end
            
            subgraph "ratelimiter"
                BUCKET4J_ADAPTER[Bucket4jRateLimiterAdapter.java]
            end
            
            subgraph "web"
                WEATHER_CONTROLLER[WeatherController.java]
            end
        end
        
        MAIN_APP[WeatherApiWrapperApplication.java]
    end
    
    %% External Dependencies
    subgraph "External Dependencies"
        SPRING_BOOT[Spring Boot]
        REDIS[Redis]
        BUCKET4J[Bucket4j]
        JACKSON[Jackson]
        WEBFLUX[WebFlux]
    end
    
    %% Package Dependencies
    domain --> application
    application --> infrastructure
    infrastructure --> domain
    
    %% Specific Dependencies
    model --> service
    service --> port
    usecase --> service
    usecase --> port
    
    cache --> port
    provider --> port
    ratelimiter --> port
    web --> usecase
    
    config --> cache
    config --> provider
    config --> ratelimiter
    config --> service
    config --> usecase
    
    MAIN_APP --> config
    
    %% External Dependencies
    infrastructure --> SPRING_BOOT
    infrastructure --> REDIS
    infrastructure --> BUCKET4J
    infrastructure --> JACKSON
    infrastructure --> WEBFLUX
    
    %% Styling
    classDef domain fill:#ffcc99,stroke:#333,stroke-width:2px
    classDef application fill:#99ff99,stroke:#333,stroke-width:2px
    classDef infrastructure fill:#99ccff,stroke:#333,stroke-width:2px
    classDef main fill:#cc99ff,stroke:#333,stroke-width:2px
    classDef external fill:#ff9999,stroke:#333,stroke-width:2px
    
    class LOCATION,WEATHER_DATA,WEATHER_REQUEST,WEATHER_RESPONSE,WEATHER_SERVICE,CACHE_SERVICE,RATE_LIMITER_SERVICE domain
    class WEATHER_PROVIDER_PORT,CACHE_PORT,RATE_LIMITER_PORT,GET_WEATHER_UC,CACHE_WEATHER_UC,RATE_LIMIT_UC application
    class REDIS_ADAPTER,APP_CONFIG,VC_PROVIDER,BUCKET4J_ADAPTER,WEATHER_CONTROLLER infrastructure
    class MAIN_APP main
    class SPRING_BOOT,REDIS,BUCKET4J,JACKSON,WEBFLUX external
```

## Package Structure

### Domain Layer (`com.shockp.weather.domain`)
The core business logic layer containing domain models and services.

#### Model Package (`com.shockp.weather.domain.model`)
- **Location.java**: Geographical location representation
- **WeatherData.java**: Weather information representation
- **WeatherRequest.java**: Weather request representation
- **WeatherResponse.java**: Weather response representation

#### Service Package (`com.shockp.weather.domain.service`)
- **WeatherService.java**: Core weather business logic
- **CacheService.java**: Cache business logic
- **RateLimiterService.java**: Rate limiting business logic

### Application Layer (`com.shockp.weather.application`)
The application layer containing ports and use cases.

#### Port Package (`com.shockp.weather.application.port`)
- **WeatherProviderPort.java**: Contract for weather data providers
- **CachePort.java**: Contract for cache operations
- **RateLimiterPort.java**: Contract for rate limiting operations

#### Use Case Package (`com.shockp.weather.application.usecase`)
- **GetWeatherUseCase.java**: Weather data retrieval use case
- **CacheWeatherUseCase.java**: Weather data caching use case
- **RateLimitUseCase.java**: Rate limiting use case

### Infrastructure Layer (`com.shockp.weather.infrastructure`)
The infrastructure layer containing adapters and external integrations.

#### Cache Package (`com.shockp.weather.infrastructure.cache`)
- **RedisCacheAdapter.java**: Redis cache implementation

#### Config Package (`com.shockp.weather.infrastructure.config`)
- **AppConfig.java**: Spring Boot configuration

#### Provider Package (`com.shockp.weather.infrastructure.provider`)
- **VisualCrossingWeatherProvider.java**: Visual Crossing API implementation

#### Rate Limiter Package (`com.shockp.weather.infrastructure.ratelimiter`)
- **Bucket4jRateLimiterAdapter.java**: Bucket4j rate limiter implementation

#### Web Package (`com.shockp.weather.infrastructure.web`)
- **WeatherController.java**: REST API controller

### Main Application
- **WeatherApiWrapperApplication.java**: Spring Boot main class

## Package Dependencies

### Dependency Rules
1. **Domain Layer**: No dependencies on other layers
2. **Application Layer**: Depends only on Domain Layer
3. **Infrastructure Layer**: Depends on Domain and Application Layers
4. **Main Application**: Depends on Infrastructure Layer

### Specific Dependencies

#### Domain Layer Dependencies
- Domain models have no external dependencies
- Domain services depend only on domain models and ports

#### Application Layer Dependencies
- Ports define contracts for infrastructure
- Use cases depend on domain services and ports

#### Infrastructure Layer Dependencies
- Adapters implement port interfaces
- Configuration creates and wires all components
- Web layer depends on use cases

## External Dependencies

### Spring Boot
- **spring-boot-starter-web**: Web application support
- **spring-boot-starter-data-redis**: Redis integration
- **spring-boot-starter-cache**: Caching support
- **spring-boot-starter-webflux**: Reactive web support

### Third-Party Libraries
- **bucket4j-core**: Rate limiting library
- **jackson-databind**: JSON processing
- **spring-boot-starter-test**: Testing support

## Package Design Principles

### Hexagonal Architecture
- **Domain**: Core business logic (innermost layer)
- **Application**: Use cases and ports (middle layer)
- **Infrastructure**: External integrations (outermost layer)

### Dependency Inversion
- High-level modules (domain) don't depend on low-level modules (infrastructure)
- Both depend on abstractions (ports)

### Package Cohesion
- Each package has a single responsibility
- Related classes are grouped together
- Clear separation of concerns

### Package Coupling
- Minimized dependencies between packages
- Clear dependency direction (domain → application → infrastructure)
- No circular dependencies

## Benefits of This Structure

1. **Maintainability**: Clear separation makes code easier to maintain
2. **Testability**: Dependencies can be easily mocked and tested
3. **Flexibility**: Easy to swap implementations (e.g., different cache providers)
4. **Scalability**: New features can be added without affecting existing code
5. **Understandability**: Clear structure makes the codebase easier to understand 