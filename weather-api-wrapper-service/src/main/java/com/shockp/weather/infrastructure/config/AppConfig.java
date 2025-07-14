package com.shockp.weather.infrastructure.config;

import com.shockp.weather.application.port.CachePort;
import com.shockp.weather.application.port.RateLimiterPort;
import com.shockp.weather.application.port.WeatherProviderPort;
import com.shockp.weather.application.usecase.cache.CacheWeatherUseCase;
import com.shockp.weather.application.usecase.weather.GetWeatherUseCase;
import com.shockp.weather.application.usecase.ratelimit.RateLimitUseCase;
import com.shockp.weather.domain.service.CacheService;
import com.shockp.weather.domain.service.RateLimiterService;
import com.shockp.weather.domain.service.WeatherService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Objects;

/**
 * {@code AppConfig} is the central Spring Boot configuration class for the Weather API Wrapper Service.
 *
 * <p>This class is responsible for wiring together all infrastructure, domain, and application layer beans
 * using dependency injection. It configures Redis caching, Bucket4j rate limiting, the Visual Crossing weather provider,
 * and all use cases. Security, validation, and best practices are enforced by delegating to the respective adapters and services.</p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *   <li>Configures and exposes beans for Redis, rate limiting, and weather provider</li>
 *   <li>Wires domain services and use cases for weather, cache, and rate limiting</li>
 *   <li>Ensures all beans are constructed with validated, secure dependencies</li>
 *   <li>Follows hexagonal architecture and layered separation</li>
 * </ul>
 *
 * <h2>Related</h2>
 * <ul>
 *   <li>{@link com.shockp.weather.infrastructure.cache.RedisCacheAdapter}</li>
 *   <li>{@link com.shockp.weather.infrastructure.ratelimiter.Bucket4jRateLimiterAdapter}</li>
 *   <li>{@link com.shockp.weather.infrastructure.provider.VisualCrossingWeatherProvider}</li>
 *   <li>{@link com.shockp.weather.domain.service.WeatherService}</li>
 *   <li>{@link com.shockp.weather.domain.service.CacheService}</li>
 *   <li>{@link com.shockp.weather.domain.service.RateLimiterService}</li>
 *   <li>{@link com.shockp.weather.application.usecase.weather.GetWeatherUseCase}</li>
 *   <li>{@link com.shockp.weather.application.usecase.cache.CacheWeatherUseCase}</li>
 *   <li>{@link com.shockp.weather.application.usecase.ratelimit.RateLimitUseCase}</li>
 * </ul>
 *
 * @author Weather API Wrapper Service
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.context.annotation.Bean
 */
@Configuration
public class AppConfig {

    /**
     * Provides a {@link WebClient} bean configured with the base URL for the Visual Crossing Weather API.
     *
     * @param builder the WebClient builder injected by Spring
     * @param baseUrl the base URL for the weather provider, injected from configuration
     * @return a configured {@link WebClient} instance
     * @throws NullPointerException if {@code baseUrl} is null
     */
    @Bean
    public WebClient webClient(WebClient.Builder builder,
                               @Value("${weather.visualcrossing.base-url}") String baseUrl) {
        Objects.requireNonNull(baseUrl, "Base URL must not be null or empty");
        return builder
                .baseUrl(baseUrl)
                .build();
    }

    /**
     * Provides a {@link RedisConnectionFactory} bean using Lettuce, configured via Spring properties.
     *
     * @param host     Redis server host (default: localhost)
     * @param port     Redis server port (default: 6379)
     * @param dbIndex  Redis database index (default: 0)
     * @param password Redis password (optional)
     * @return a configured {@link LettuceConnectionFactory} instance
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory(
            @Value("${spring.redis.host:localhost}") String host,
            @Value("${spring.redis.port:6379}") int port,
            @Value("${spring.redis.database:0}") int dbIndex,
            @Value("${spring.redis.password:}") String password) {

        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host, port);
        config.setDatabase(dbIndex);
        if (password != null && !password.isBlank()) {
            config.setPassword(password.trim());
        }
        return new LettuceConnectionFactory(config);
    }

    /**
     * Provides a {@link RedisTemplate} bean for String keys and values (JSON payloads).
     *
     * @param factory the Redis connection factory
     * @return a configured {@link RedisTemplate} instance
     */
    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        // Use String serialization for keys and values
        template.setKeySerializer(template.getStringSerializer());
        template.setValueSerializer(template.getStringSerializer());
        template.afterPropertiesSet();
        return template;
    }

    /**
     * Provides a {@link CacheService} bean, wiring the {@link CachePort} adapter with a default TTL.
     *
     * @param cachePort    the cache port adapter
     * @param cacheTimeout the cache timeout duration (default: PT30M)
     * @return a configured {@link CacheService} instance
     * @throws NullPointerException     if {@code cacheTimeout} is null
     * @throws IllegalArgumentException if {@code cacheTimeout} is zero or negative
     */
    @Bean
    public CacheService cacheService(CachePort cachePort,
                                     @Value("${weather.cache.timeout:PT30M}") Duration cacheTimeout) {
        Objects.requireNonNull(cacheTimeout, "Cache timeout must not be null");
        if (cacheTimeout.isZero() || cacheTimeout.isNegative()) {
            throw new IllegalArgumentException("Cache timeout must be positive");
        }
        return new CacheService(cachePort, cacheTimeout);
    }

    /**
     * Provides a {@link RateLimiterService} bean, wiring the {@link RateLimiterPort} adapter.
     *
     * @param rateLimiterPort the rate limiter port adapter
     * @param maxRequests     the maximum number of requests allowed in the time window (default: 100)
     * @param timeWindow      the time window for rate limiting (default: PT1H)
     * @return a configured {@link RateLimiterService} instance
     * @throws IllegalArgumentException if {@code maxRequests} is not positive or {@code timeWindow} is zero/negative
     * @throws NullPointerException     if {@code timeWindow} is null
     */
    @Bean
    public RateLimiterService rateLimiterService(RateLimiterPort rateLimiterPort,
                                                 @Value("${rate.limit.max-requests:100}") int maxRequests,
                                                 @Value("${rate.limit.time-window:PT1H}") Duration timeWindow) {
        if (maxRequests <= 0) {
            throw new IllegalArgumentException("Max requests must be positive");
        }
        Objects.requireNonNull(timeWindow, "Time window must not be null");
        if (timeWindow.isZero() || timeWindow.isNegative()) {
            throw new IllegalArgumentException("Time window must be positive");
        }
        return new RateLimiterService(rateLimiterPort, maxRequests, timeWindow);
    }

    /**
     * Provides a {@link WeatherService} bean, wiring the weather provider and cache services.
     *
     * @param provider     the weather provider port
     * @param cacheService the cache service
     * @return a configured {@link WeatherService} instance
     */
    @Bean
    public WeatherService weatherService(WeatherProviderPort provider,
                                         CacheService cacheService) {
        return new WeatherService(provider, cacheService);
    }

    /**
     * Provides a {@link GetWeatherUseCase} bean, delegating to {@link WeatherService}.
     *
     * @param weatherService the weather service
     * @return a configured {@link GetWeatherUseCase} instance
     */
    @Bean
    public GetWeatherUseCase getWeatherUseCase(WeatherService weatherService) {
        return new GetWeatherUseCase(weatherService);
    }

    /**
     * Provides a {@link CacheWeatherUseCase} bean, delegating to {@link CacheService}.
     *
     * @param cacheService the cache service
     * @return a configured {@link CacheWeatherUseCase} instance
     */
    @Bean
    public CacheWeatherUseCase cacheWeatherUseCase(CacheService cacheService) {
        return new CacheWeatherUseCase(cacheService);
    }

    /**
     * Provides a {@link RateLimitUseCase} bean, delegating to {@link RateLimiterService}.
     *
     * @param rateLimiterService the rate limiter service
     * @return a configured {@link RateLimitUseCase} instance
     */
    @Bean
    public RateLimitUseCase rateLimitUseCase(RateLimiterService rateLimiterService) {
        return new RateLimitUseCase(rateLimiterService);
    }
}
