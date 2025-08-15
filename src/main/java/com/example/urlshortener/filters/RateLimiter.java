package com.example.urlshortener.filters;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class RateLimiter {
    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private static final int MAX_REQUESTS = 100;
    private static final Duration WINDOW = Duration.ofMinutes(1);

    @Value("${load.test.enabled}")
    private boolean loadTestEnabled;

    public RateLimiter(ReactiveRedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Mono<Boolean> isAllowed(String clientIp) {
        if (loadTestEnabled) {
            // Bypass rate limiting during load tests
            return Mono.just(true);
        }
        String key = "rate:limit:" + clientIp;
        return redisTemplate.opsForValue().increment(key)
                .flatMap(count -> redisTemplate.expire(key, WINDOW)
                        .thenReturn(count <= MAX_REQUESTS));
    }
}