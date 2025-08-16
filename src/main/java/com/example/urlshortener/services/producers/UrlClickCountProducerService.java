package com.example.urlshortener.services.producers;

import com.example.urlshortener.configs.props.RabbitProps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UrlClickCountProducerService {
    private final RabbitTemplate rabbitTemplate;
    private final RabbitProps rabbitProps;

    public void produceIncrementClickCountUpdateToQueue(String shortCode) {
        String message = String.format("URL with short code '%s' clicked", shortCode);
        log.info(message);
        rabbitTemplate.convertAndSend(
                rabbitProps.getExchangeName(),
                rabbitProps.getRoutingKey(),
                shortCode
        );
    }
}
