package com.device.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.device.service.DeviceService;

@Configuration
public class RabbitMQConfig {

    @Bean
    public Queue locationUpdateQueue() {
        return new Queue("location-update-queue", true);
    }

    @Bean
    public FanoutExchange locationExchange() { // O DirectExchange según tu configuración
        return new FanoutExchange("location-exchange");
    }

    @Bean
    public Binding locationBinding(Queue locationUpdateQueue, FanoutExchange locationExchange) {
        return BindingBuilder.bind(locationUpdateQueue).to(locationExchange);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        return rabbitTemplate;
    }
    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public SimpleMessageListenerContainer container(ConnectionFactory connectionFactory, 
                                                    MessageListenerAdapter listenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames("location-update-queue");
        container.setMessageListener(listenerAdapter);
        return container;
    }

    @Bean
    public MessageListenerAdapter listenerAdapter(DeviceService deviceService) {
        MessageListenerAdapter adapter = new MessageListenerAdapter(deviceService, "updateDeviceStatus");
        adapter.setMessageConverter(jackson2JsonMessageConverter());
        return adapter;
    }
}