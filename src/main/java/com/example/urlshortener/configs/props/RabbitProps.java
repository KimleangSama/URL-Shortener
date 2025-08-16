package com.example.urlshortener.configs.props;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Setter
@ToString
@Component
public class RabbitProps {
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
    @Value("${spring.rabbitmq.host}")
    public String host;
    @Value("${spring.rabbitmq.port}")
    public int port;
    @Value("${spring.rabbitmq.username}")
    public String username;
    @Value("${spring.rabbitmq.password}")
    public String password;
}
