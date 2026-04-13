package com.marketplace.api_gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;

// GlobalFilter se aplica a TODAS las rutas automáticamente
// Ordered define la prioridad — menor número = se ejecuta antes
@Slf4j
@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        long startTime = Instant.now().toEpochMilli();

        log.info("→ {} {} [{}]",
                request.getMethod(),
                request.getPath(),
                request.getRemoteAddress()
        );

        // then() se ejecuta cuando el filtro termina
        // Equivale a un "finally" asíncrono
        return chain.filter(exchange).then(
                Mono.fromRunnable(() -> {
                    long duration = Instant.now().toEpochMilli() - startTime;
                    log.info("← {} {} {}ms",
                            exchange.getResponse().getStatusCode(),
                            request.getPath(),
                            duration
                    );
                })
        );
    }

    @Override
    public int getOrder() {
        return -1; // prioridad alta — se ejecuta primero
    }
}