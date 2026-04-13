package com.marketplace.api_gateway.filter;

import com.marketplace.api_gateway.security.JwtService;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

// AbstractGatewayFilterFactory es la forma correcta de crear filtros
// con configuración en Spring Cloud Gateway
@Slf4j
@Component
public class AuthenticationFilter
        extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Autowired
    private JwtService jwtService;

    public AuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        // La lambda recibe el exchange (request+response) y la chain
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            // Extraemos el Authorization header
            String authHeader = request.getHeaders()
                    .getFirst("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Missing or invalid Authorization header for: {}",
                        request.getPath());
                return onError(exchange, HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);

            if (!jwtService.isTokenValid(token)) {
                log.warn("Invalid JWT token for: {}", request.getPath());
                return onError(exchange, HttpStatus.UNAUTHORIZED);
            }

            // Extraemos los claims del token
            Claims claims = jwtService.extractAllClaims(token);
            String userId = claims.get("userId", String.class);
            String role = claims.get("role", String.class);
            String email = claims.getSubject();

            // Si el userId no está en los claims, lo marcamos como vacío
            // Los servicios que lo necesiten deberán buscarlo por email
            if (userId == null) userId = "";

            log.debug("Authenticated request - userId: {}, role: {}, path: {}",
                    userId, role, request.getPath());

            // Mutamos el request añadiendo headers con la info del usuario
            // Los microservicios leen estos headers en vez de verificar el JWT
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-User-Id", userId)
                    .header("X-User-Email", email)
                    .header("X-User-Role", role != null ? role : "")
                    .build();

            // Continuamos la cadena con el request modificado
            return chain.filter(exchange.mutate()
                    .request(mutatedRequest)
                    .build());
        };
    }

    // Mono<Void> es el tipo reactivo equivalente a void en programación reactiva
    // En vez de bloquear el hilo, devuelve una promesa de "nada"
    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        return response.setComplete();
    }

    // Config es la clase de configuración del filtro
    // Permite pasar parámetros desde el YAML si es necesario
    public static class Config {
        // Por ahora sin configuración adicional
        // Se puede extender para añadir roles requeridos, etc.
    }
}