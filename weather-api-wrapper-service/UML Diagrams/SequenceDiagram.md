# Sequence Diagram - Weather API Wrapper Service

## Overview
This diagram shows the sequence of interactions when a client requests weather data, including caching, rate limiting, and external API calls.

## Main Weather Data Retrieval Sequence

```mermaid
sequenceDiagram
    participant Client as HTTP Client
    participant Controller as WeatherController
    participant RateLimitUC as RateLimitUseCase
    participant CacheUC as CacheWeatherUseCase
    participant GetWeatherUC as GetWeatherUseCase
    participant WeatherService as WeatherService
    participant CacheService as CacheService
    participant RateLimiterService as RateLimiterService
    participant CachePort as CachePort
    participant RateLimiterPort as RateLimiterPort
    participant WeatherProviderPort as WeatherProviderPort
    participant Redis as Redis Cache
    participant VC_API as Visual Crossing API

    %% Initial Request
    Client->>Controller: GET /api/weather?location=London&date=2024-01-15
    Controller->>Controller: extractClientId(request)
    Controller->>Controller: validateLocation(location)
    Controller->>Controller: validateDate(date)
    
    %% Rate Limiting Check
    Controller->>RateLimitUC: execute(clientId)
    RateLimitUC->>RateLimiterService: checkRateLimit(clientId)
    RateLimiterService->>RateLimiterPort: tryConsume(clientId)
    RateLimiterPort-->>RateLimiterService: true/false
    RateLimiterService-->>RateLimitUC: boolean
    RateLimitUC-->>Controller: boolean
    
    alt Rate Limit Exceeded
        Controller-->>Client: 429 Too Many Requests
    else Rate Limit OK
        %% Create Weather Request
        Controller->>Controller: create WeatherRequest(location, date, includeHourly)
        
        %% Check Cache First
        Controller->>CacheUC: retrieve(cacheKey)
        CacheUC->>CacheService: get(cacheKey)
        CacheService->>CachePort: get(cacheKey)
        CachePort->>Redis: GET cacheKey
        Redis-->>CachePort: WeatherData/null
        CachePort-->>CacheService: Optional<WeatherData>
        CacheService-->>CacheUC: Optional<WeatherData>
        CacheUC-->>Controller: Optional<WeatherData>
        
        alt Cache Hit
            Controller->>Controller: create WeatherResponse(cachedData, true)
            Controller-->>Client: 200 OK + WeatherResponse
        else Cache Miss
            %% Get Weather Data from Provider
            Controller->>GetWeatherUC: execute(weatherRequest)
            GetWeatherUC->>WeatherService: getWeather(request)
            WeatherService->>WeatherService: validateRequest(request)
            WeatherService->>WeatherProviderPort: getWeatherData(request)
            WeatherProviderPort->>VC_API: GET /timeline/{location}/{date}
            VC_API-->>WeatherProviderPort: JSON Response
            WeatherProviderPort->>WeatherProviderPort: parseResponse(response)
            WeatherProviderPort-->>WeatherService: WeatherData
            WeatherService-->>GetWeatherUC: WeatherResponse
            GetWeatherUC-->>Controller: WeatherResponse
            
            %% Cache the New Data
            Controller->>CacheUC: execute(cacheKey, weatherData)
            CacheUC->>CacheService: put(cacheKey, weatherData)
            CacheService->>CachePort: put(cacheKey, weatherData, ttl)
            CachePort->>Redis: SET cacheKey weatherData TTL
            Redis-->>CachePort: OK
            CachePort-->>CacheService: void
            CacheService-->>CacheUC: void
            CacheUC-->>Controller: void
            
            Controller-->>Client: 200 OK + WeatherResponse
        end
    end
```

## Cache Management Sequence

```mermaid
sequenceDiagram
    participant Client as HTTP Client
    participant Controller as WeatherController
    participant CacheUC as CacheWeatherUseCase
    participant CacheService as CacheService
    participant CachePort as CachePort
    participant Redis as Redis Cache

    %% Get Cache Status
    Client->>Controller: GET /api/cache/status/{key}
    Controller->>CacheUC: retrieve(key)
    CacheUC->>CacheService: get(key)
    CacheService->>CachePort: exists(key)
    CachePort->>Redis: EXISTS key
    Redis-->>CachePort: boolean
    CachePort-->>CacheService: boolean
    CacheService-->>CacheUC: Optional<WeatherData>
    CacheUC-->>Controller: Optional<WeatherData>
    Controller-->>Client: 200 OK + CacheStatus

    %% Clear Cache
    Client->>Controller: DELETE /api/cache/{key}
    Controller->>CacheUC: invalidate(key)
    CacheUC->>CacheService: evict(key)
    CacheService->>CachePort: delete(key)
    CachePort->>Redis: DEL key
    Redis-->>CachePort: integer
    CachePort-->>CacheService: void
    CacheService-->>CacheUC: void
    CacheUC-->>Controller: void
    Controller-->>Client: 204 No Content
```

## Rate Limiting Management Sequence

```mermaid
sequenceDiagram
    participant Client as HTTP Client
    participant Controller as WeatherController
    participant RateLimitUC as RateLimitUseCase
    participant RateLimiterService as RateLimiterService
    participant RateLimiterPort as RateLimiterPort

    %% Get Rate Limit Status
    Client->>Controller: GET /api/rate-limit/status/{clientId}
    Controller->>RateLimitUC: getRemainingRequests(clientId)
    RateLimitUC->>RateLimiterService: getRemainingTokens(clientId)
    RateLimiterService->>RateLimiterPort: getAvailableTokens(clientId)
    RateLimiterPort-->>RateLimiterService: int
    RateLimiterService-->>RateLimitUC: int
    RateLimitUC-->>Controller: int
    Controller-->>Client: 200 OK + RateLimitStatus

    %% Reset Rate Limit
    Client->>Controller: POST /api/rate-limit/reset/{clientId}
    Controller->>RateLimitUC: resetLimit(clientId)
    RateLimitUC->>RateLimiterService: reset(clientId)
    RateLimiterService->>RateLimiterPort: reset(clientId)
    RateLimiterPort-->>RateLimiterService: void
    RateLimiterService-->>RateLimitUC: void
    RateLimitUC-->>Controller: void
    Controller-->>Client: 200 OK
```

## Error Handling Sequence

```mermaid
sequenceDiagram
    participant Client as HTTP Client
    participant Controller as WeatherController
    participant GetWeatherUC as GetWeatherUseCase
    participant WeatherService as WeatherService
    participant WeatherProviderPort as WeatherProviderPort
    participant VC_API as Visual Crossing API

    %% Validation Error
    Client->>Controller: GET /api/weather?location=invalid&date=invalid
    Controller->>Controller: validateLocation(location)
    Controller->>Controller: handleValidationException(exception)
    Controller-->>Client: 400 Bad Request + ErrorResponse

    %% External API Error
    Client->>Controller: GET /api/weather?location=London&date=2024-01-15
    Controller->>GetWeatherUC: execute(weatherRequest)
    GetWeatherUC->>WeatherService: getWeather(request)
    WeatherService->>WeatherProviderPort: getWeatherData(request)
    WeatherProviderPort->>VC_API: GET /timeline/{location}/{date}
    VC_API-->>WeatherProviderPort: 500 Internal Server Error
    WeatherProviderPort->>WeatherProviderPort: handleApiError(exception)
    WeatherProviderPort-->>WeatherService: WeatherServiceException
    WeatherService-->>GetWeatherUC: WeatherServiceException
    GetWeatherUC->>GetWeatherUC: handleError(exception)
    GetWeatherUC-->>Controller: WeatherResponse (with error)
    Controller->>Controller: handleWeatherServiceException(exception)
    Controller-->>Client: 503 Service Unavailable + ErrorResponse

    %% Rate Limit Error
    Client->>Controller: GET /api/weather?location=London&date=2024-01-15
    Controller->>Controller: checkRateLimit(clientId)
    Controller->>Controller: handleRateLimitException(exception)
    Controller-->>Client: 429 Too Many Requests + ErrorResponse
```

## Key Interaction Patterns

### 1. **Request Flow**
1. **Client Request**: HTTP client sends request to controller
2. **Validation**: Input validation and client identification
3. **Rate Limiting**: Check if client has available tokens
4. **Cache Check**: Look for cached weather data
5. **External Call**: If cache miss, call external weather API
6. **Cache Storage**: Store new data in cache
7. **Response**: Return weather data to client

### 2. **Caching Strategy**
- **Cache-First**: Always check cache before external API calls
- **TTL Management**: Automatic expiration of cached data
- **Cache Invalidation**: Manual cache clearing capabilities
- **Cache Status**: Monitoring cache hit/miss statistics

### 3. **Rate Limiting Strategy**
- **Token Bucket**: Bucket4j-based rate limiting
- **Per-Client**: Individual rate limits per client ID
- **Graceful Degradation**: Clear error messages when limits exceeded
- **Reset Capability**: Manual rate limit reset functionality

### 4. **Error Handling**
- **Validation Errors**: Input validation with clear error messages
- **External API Errors**: Graceful handling of third-party service failures
- **Rate Limit Errors**: Clear indication when limits are exceeded
- **System Errors**: Proper error responses with appropriate HTTP status codes

## Performance Considerations

### 1. **Caching Benefits**
- **Reduced Latency**: Cached responses are faster than API calls
- **Reduced Load**: Fewer external API calls reduce load on third-party services
- **Cost Savings**: Fewer API calls mean lower costs
- **Reliability**: Cached data available even when external API is down

### 2. **Rate Limiting Benefits**
- **API Protection**: Prevents abuse and ensures fair usage
- **Cost Control**: Limits external API costs
- **Service Stability**: Prevents overwhelming external services
- **Compliance**: Ensures adherence to API usage limits

### 3. **Error Resilience**
- **Graceful Degradation**: Service continues to work with cached data
- **Clear Error Messages**: Users understand what went wrong
- **Retry Logic**: Automatic retry mechanisms for transient failures
- **Monitoring**: Comprehensive error tracking and alerting 