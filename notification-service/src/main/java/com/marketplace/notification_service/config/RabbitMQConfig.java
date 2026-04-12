package com.marketplace.notification_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange}")
    private String exchange;

    @Value("${rabbitmq.queues.order-created}")
    private String orderCreatedQueue;

    @Value("${rabbitmq.queues.stock-reserved}")
    private String stockReservedQueue;

    @Value("${rabbitmq.queues.stock-failed}")
    private String stockFailedQueue;

    @Value("${rabbitmq.queues.order-paid}")
    private String orderPaidQueue;

    @Value("${rabbitmq.queues.dlq.order-created}")
    private String orderCreatedDlq;

    @Value("${rabbitmq.queues.dlq.stock-reserved}")
    private String stockReservedDlq;

    @Value("${rabbitmq.queues.dlq.stock-failed}")
    private String stockFailedDlq;

    @Bean
    public TopicExchange marketplaceExchange() {
        return new TopicExchange(exchange);
    }

    // Exchange especial para los mensajes muertos
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange("marketplace.dlx");
    }

    // Queue con DLQ configurada
    // x-dead-letter-exchange → dónde van los mensajes que fallan
    // x-dead-letter-routing-key → routing key del mensaje fallido
    @Bean
    public Queue orderCreatedQueue() {
        return QueueBuilder.durable(orderCreatedQueue)
                .withArgument("x-dead-letter-exchange", "marketplace.dlx")
                .withArgument("x-dead-letter-routing-key", orderCreatedDlq)
                .build();
    }

    @Bean
    public Queue stockReservedQueue() {
        return QueueBuilder.durable(stockReservedQueue)
                .withArgument("x-dead-letter-exchange", "marketplace.dlx")
                .withArgument("x-dead-letter-routing-key", stockReservedDlq)
                .build();
    }

    @Bean
    public Queue stockFailedQueue() {
        return QueueBuilder.durable(stockFailedQueue)
                .withArgument("x-dead-letter-exchange", "marketplace.dlx")
                .withArgument("x-dead-letter-routing-key", stockFailedDlq)
                .build();
    }

    @Bean
    public Queue orderPaidQueue() {
        return QueueBuilder.durable(orderPaidQueue).build();
    }

    // Las DLQs son queues normales — sin DLQ propia para simplicidad
    @Bean
    public Queue orderCreatedDlqQueue() {
        return new Queue(orderCreatedDlq, true);
    }

    @Bean
    public Queue stockReservedDlqQueue() {
        return new Queue(stockReservedDlq, true);
    }

    @Bean
    public Queue stockFailedDlqQueue() {
        return new Queue(stockFailedDlq, true);
    }

    // Bindings normales
    @Bean
    public Binding orderCreatedBinding() {
        return BindingBuilder.bind(orderCreatedQueue())
                .to(marketplaceExchange()).with("order.created");
    }

    @Bean
    public Binding stockReservedBinding() {
        return BindingBuilder.bind(stockReservedQueue())
                .to(marketplaceExchange()).with("stock.reserved");
    }

    @Bean
    public Binding stockFailedBinding() {
        return BindingBuilder.bind(stockFailedQueue())
                .to(marketplaceExchange()).with("stock.reservation.failed");
    }

    @Bean
    public Binding orderPaidBinding() {
        return BindingBuilder.bind(orderPaidQueue())
                .to(marketplaceExchange()).with("order.paid");
    }

    // Bindings de DLQ
    @Bean
    public Binding orderCreatedDlqBinding() {
        return BindingBuilder.bind(orderCreatedDlqQueue())
                .to(deadLetterExchange()).with(orderCreatedDlq);
    }

    @Bean
    public Binding stockReservedDlqBinding() {
        return BindingBuilder.bind(stockReservedDlqQueue())
                .to(deadLetterExchange()).with(stockReservedDlq);
    }

    @Bean
    public Binding stockFailedDlqBinding() {
        return BindingBuilder.bind(stockFailedDlqQueue())
                .to(deadLetterExchange()).with(stockFailedDlq);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}