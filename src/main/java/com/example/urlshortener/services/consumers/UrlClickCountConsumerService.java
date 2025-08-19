package com.example.urlshortener.services.consumers;

import com.example.urlshortener.configs.props.RabbitProps;
import com.example.urlshortener.repos.UrlRepository;
import com.example.urlshortener.services.RedisService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.Receiver;

@Slf4j
@Service
public class UrlClickCountConsumerService {
    private final Receiver receiver;
    private final UrlRepository urlRepository;
    private final RedisService redisService;
    private final RabbitProps rabbitProps;

    public UrlClickCountConsumerService(
            UrlRepository urlRepository,
            RedisService redisService,
            RabbitProps rabbitProps,
            Receiver receiver) {
        this.urlRepository = urlRepository;
        this.redisService = redisService;
        this.rabbitProps = rabbitProps;
        this.receiver = receiver;
    }

    @PostConstruct
    public void init() {
        if (rabbitProps == null) {
            throw new IllegalStateException("RabbitProps is not initialized");
        }
    }

    @PreDestroy
    public void cleanup() {
//        if (sender != null) sender.close();
        if (receiver != null) receiver.close();
    }

    @EventListener(ContextRefreshedEvent.class)
    public void startConsuming() {
        receiver.consumeAutoAck(rabbitProps.getQueueName())
                .limitRate(10)
                .onBackpressureBuffer(100)
                .flatMap(msg -> {
                    String shortCode = new String(msg.getBody());
                    log.info("Received message from queue: {}", shortCode);
                    return processMessage(shortCode)
                            .thenReturn(shortCode);
                }, 10)
                .doOnNext(shortCode -> log.info("Processed click count update for short code: {}", shortCode))
                .doOnError(e -> log.error("Error consuming messages", e))
                .subscribe(); // crucial!
    }

    private Mono<Void> processMessage(String shortCode) {
        return redisService.incrementClickCount(shortCode)
                .then(urlRepository.findByShortCode(shortCode)
                        .flatMap(entity -> {
                            entity.setClickCount(entity.getClickCount() + 1);
                            return urlRepository.save(entity);
                        })
                ).then();
    }
}
