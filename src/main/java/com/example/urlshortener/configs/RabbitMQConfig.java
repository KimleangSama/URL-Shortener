package com.example.urlshortener.configs;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RabbitMQConfig {
    @Value("${queue.url-shortener.name}")
    private String queueName;
    @Value("${queue.url-shortener.exchange-name}")
    private String exchangeName;
    @Value("${queue.url-shortener.routing-key}")
    private String routingKey;
    @Value("${queue.url-shortener.durable}")
    private boolean durable;
    @Value("${queue.url-shortener.auto-delete}")
    private boolean autoDelete;

    @Bean
    public Queue urlClickCountQueue() {
        return new Queue(queueName, durable, false, autoDelete);
    }

    @Bean
    public TopicExchange urlClickCountExchange() {
        return new TopicExchange(exchangeName, durable, autoDelete);
    }

    @Bean
    public Binding binding(Queue urlClickCountQueue, TopicExchange urlClickCountExchange) {
        return BindingBuilder
                .bind(urlClickCountQueue)
                .to(urlClickCountExchange)
                .with(routingKey);
    }
}
