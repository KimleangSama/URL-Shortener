package com.example.urlshortener.services;

import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class RedisService {
    private final ReactiveRedisTemplate<String, String> redisTemplate;

    public RedisService(ReactiveRedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Mono<String> getUrl(String shortCode) {
        return redisTemplate.opsForValue().get("url:" + shortCode);
    }

    public Mono<Boolean> setUrl(String shortCode, String longUrl, Duration ttl) {
        return redisTemplate.opsForValue().set("url:" + shortCode, longUrl, ttl);
    }

    public Mono<Long> incrementClickCount(String shortCode) {
        return redisTemplate.opsForValue().increment("clicks:" + shortCode);
    }
}