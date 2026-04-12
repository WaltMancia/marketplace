package com.marketplace.orderservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

    // Exchange — el enrutador de mensajes
    // TopicExchange permite patrones con wildcards en las routing keys
    // Ej: "order.*" captura "order.created", "order.paid", etc.
    @Bean
    public TopicExchange marketplaceExchange() {
        return new TopicExchange(exchange);
    }

    // Declaramos las queues — si no existen, RabbitMQ las crea
    @Bean
    public Queue orderCreatedQueue() {
        // durable=true → la queue sobrevive al reinicio de RabbitMQ
        return new Queue(orderCreatedQueue, true);
    }

    @Bean
    public Queue stockReservedQueue() {
        return new Queue(stockReservedQueue, true);
    }

    @Bean
    public Queue stockFailedQueue() {
        return new Queue(stockFailedQueue, true);
    }

    @Bean
    public Queue orderPaidQueue() {
        return new Queue(orderPaidQueue, true);
    }

    // Bindings — conectan queues con el exchange usando routing keys
    @Bean
    public Binding orderCreatedBinding() {
        return BindingBuilder
                .bind(orderCreatedQueue())
                .to(marketplaceExchange())
                .with("order.created");
    }

    @Bean
    public Binding stockReservedBinding() {
        return BindingBuilder
                .bind(stockReservedQueue())
                .to(marketplaceExchange())
                .with("stock.reserved");
    }

    @Bean
    public Binding stockFailedBinding() {
        return BindingBuilder
                .bind(stockFailedQueue())
                .to(marketplaceExchange())
                .with("stock.reservation.failed");
    }

    @Bean
    public Binding orderPaidBinding() {
        return BindingBuilder
                .bind(orderPaidQueue())
                .to(marketplaceExchange())
                .with("order.paid");
    }

    // Convertidor de mensajes — serializa/deserializa objetos Java a JSON
    // Sin esto RabbitMQ usaría serialización binaria de Java
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // RabbitTemplate es el cliente para enviar mensajes
    // Lo configuramos para usar JSON
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}