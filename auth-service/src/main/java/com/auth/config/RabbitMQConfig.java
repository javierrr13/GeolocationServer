package com.auth.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public Queue permissionCheckQueue() {
        return new Queue("permission-check-queue", true);
    }

    @Bean
    public DirectExchange permissionExchange() {
        return new DirectExchange("permission-exchange");
    }

    @Bean
    public Binding permissionBinding(Queue permissionCheckQueue, DirectExchange permissionExchange) {
        return BindingBuilder.bind(permissionCheckQueue).to(permissionExchange).with("permission.check");
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        return rabbitTemplate;
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}