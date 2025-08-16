package com.example.urlshortener.services.consumers;

import com.example.urlshortener.configs.props.RabbitProps;
import com.example.urlshortener.repos.UrlRepository;
import com.example.urlshortener.services.RedisService;
import com.rabbitmq.client.ConnectionFactory;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.RabbitFlux;
import reactor.rabbitmq.Receiver;
import reactor.rabbitmq.ReceiverOptions;

@Slf4j
@Service
public class UrlClickCountConsumerService {
    private final UrlRepository urlRepository;
    private final RedisService redisService;
    private final RabbitProps rabbitProps;
    private final ConnectionFactory connectionFactory;

    public UrlClickCountConsumerService(
            UrlRepository urlRepository,
            RedisService redisService,
            RabbitProps rabbitProps,
            @Qualifier("customRabbitConnectionFactory") ConnectionFactory connectionFactory) {
        this.urlRepository = urlRepository;
        this.redisService = redisService;
        this.rabbitProps = rabbitProps;
        this.connectionFactory = connectionFactory;
    }

    private Receiver receiver;

    @PostConstruct
    public void init() {
        if (rabbitProps == null) {
            throw new IllegalStateException("RabbitProps is not initialized");
        }
        this.receiver = RabbitFlux.createReceiver(new ReceiverOptions().connectionFactory(connectionFactory));
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
                })
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
