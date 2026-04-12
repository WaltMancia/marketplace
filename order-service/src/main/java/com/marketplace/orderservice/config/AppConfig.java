package com.marketplace.orderservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    // RestTemplate es el cliente HTTP de Spring
    // Lo declaramos como Bean para que Spring lo gestione
    // y podamos inyectarlo donde lo necesitemos
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}