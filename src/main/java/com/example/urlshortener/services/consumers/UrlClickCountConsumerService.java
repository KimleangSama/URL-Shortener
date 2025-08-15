package com.example.urlshortener.services.consumers;

import com.example.urlshortener.configs.props.RabbitMQProps;
import com.example.urlshortener.repos.UrlRepository;
import com.example.urlshortener.services.RedisService;
import com.rabbitmq.client.ConnectionFactory;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.RabbitFlux;
import reactor.rabbitmq.Receiver;
import reactor.rabbitmq.ReceiverOptions;

@Slf4j
@Service
@RequiredArgsConstructor
public class UrlClickCountConsumerService {
    private final UrlRepository urlRepository;
    private final RedisService redisService;
    private final RabbitMQProps rabbitMQProps;

    private Receiver receiver;

    @PostConstruct
    public void init() {
        if (rabbitMQProps == null) {
            throw new IllegalStateException("RabbitMQProps is not initialized");
        }
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(rabbitMQProps.getHost());
        connectionFactory.setPort(rabbitMQProps.getPort());
        connectionFactory.setUsername(rabbitMQProps.getUsername());
        connectionFactory.setPassword(rabbitMQProps.getPassword());
        this.receiver = RabbitFlux.createReceiver(new ReceiverOptions().connectionFactory(connectionFactory));
    }

    @PreDestroy
    public void cleanup() {
//        if (sender != null) sender.close();
        if (receiver != null) receiver.close();
    }

    @EventListener(ContextRefreshedEvent.class)
    public void startConsuming() {
        receiver.consumeAutoAck(rabbitMQProps.getQueueName())
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
