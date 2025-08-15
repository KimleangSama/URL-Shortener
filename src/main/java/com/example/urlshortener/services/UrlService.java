package com.example.urlshortener.services;

import com.example.urlshortener.entities.UrlEntity;
import com.example.urlshortener.repos.UrlRepository;
import com.example.urlshortener.services.producers.UrlClickCountProducerService;
import com.example.urlshortener.utils.Base62Encoder;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class UrlService {
    private final UrlRepository urlRepository;
    private final RedisService redisService;
    private final Base62Encoder encoder;
    private final ReactiveCircuitBreaker reactiveCircuitBreaker;
    private final UrlClickCountProducerService urlClickCountProducerService;

    public UrlService(UrlRepository urlRepository, RedisService redisService,
                      Base62Encoder encoder, ReactiveCircuitBreakerFactory<?, ?> circuitBreakerFactory,
                      UrlClickCountProducerService urlClickCountProducerService
    ) {
        this.urlRepository = urlRepository;
        this.redisService = redisService;
        this.encoder = encoder;
        this.reactiveCircuitBreaker = circuitBreakerFactory.create("urlServiceCircuitBreaker");
        this.urlClickCountProducerService = urlClickCountProducerService;
    }

    public Mono<String> shortenUrl(String longUrl) {
        return Mono.just(longUrl)
                .filter(this::isValidUrl)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Invalid URL")))
                .flatMap(url -> {
                    UrlEntity entity = new UrlEntity();
                    entity.setLongUrl(url);
                    String shortCode = encoder.generateCode();
                    entity.setShortCode(shortCode);
                    entity.setCreatedAt(Instant.now());
                    entity.setClickCount(0L);
                    return urlRepository.save(entity) // Update with shortCode
                            .then(redisService.setUrl(entity.getShortCode(), url, Duration.ofDays(30))
                                    .thenReturn(entity.getShortCode()));
                });
    }

    public Mono<String> getLongUrl(String shortCode) {
        Mono<String> logic = redisService.getUrl(shortCode)
                .doOnNext(entity -> {
                    if (entity == null) {
                        log.warn("Short code {} not found in Redis cache", shortCode);
                    } else {
                        log.info("Retrieved long URL from Redis for short code: {}", shortCode);
                    }
                })
                .switchIfEmpty(urlRepository.findByShortCode(shortCode)
                        .flatMap(entity -> redisService.setUrl(shortCode, entity.getLongUrl(), Duration.ofDays(30))
                                .thenReturn(entity.getLongUrl())
                        )
                )
                .flatMap(url -> Mono.fromRunnable(() ->
                        urlClickCountProducerService.produceIncrementClickCountUpdateToQueue(shortCode))
                );

        return reactiveCircuitBreaker.run(logic, throwable -> {
            log.warn("Fallback triggered due to: {}", throwable.toString());
            return Mono.empty(); // or any fallback Mono
        });
    }

    private boolean isValidUrl(String url) {
        try {
            URL parsedUrl = URI.create(url).toURL();
            String protocol = parsedUrl.getProtocol();
            return protocol.equalsIgnoreCase("http") || protocol.equalsIgnoreCase("https");
        } catch (MalformedURLException e) {
            return false;
        }
    }

}
