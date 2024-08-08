package com.jdkendall.bankaudit.api.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.ContainerCustomizer;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.amqp.RabbitTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    @Bean
    RabbitTemplateCustomizer rabbitTemplateCustomizer() {
        return (template) -> template.setObservationEnabled(true);
    }

    @Bean
    ContainerCustomizer<SimpleMessageListenerContainer> containerCustomizer() {
        return (container) -> container.setObservationEnabled(true);
    }

    @Bean
    public Queue pendingQueue(@Value("${spring.rabbitmq.queue.pending.all}") String name) {
        return new Queue(name, true);
    }

    @Bean
    public DirectExchange exchange(@Value("${spring.rabbitmq.exchange}") String name) {
        return new DirectExchange(name, true, false);
    }

    @Bean
    public Binding binding(Queue pendingQueue, DirectExchange exchange, @Value("${spring.rabbitmq.queue.pending.all}") String routingKey) {
        return BindingBuilder.bind(pendingQueue).to(exchange).with(routingKey);
    }
}
