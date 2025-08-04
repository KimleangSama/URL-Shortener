package com.example.urlshortener;

import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class RateLimiter {
    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private static final int MAX_REQUESTS = 100;
    private static final Duration WINDOW = Duration.ofMinutes(1);

    public RateLimiter(ReactiveRedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Mono<Boolean> isAllowed(String clientIp) {
        String key = "rate:limit:" + clientIp;
        return redisTemplate.opsForValue().increment(key)
                .flatMap(count -> redisTemplate.expire(key, WINDOW)
                        .thenReturn(count <= MAX_REQUESTS));
    }
}