package com.marketplace.orderservice.security;

import com.marketplace.orderservice.security.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// OncePerRequestFilter garantiza que este filtro se ejecuta
// exactamente UNA vez por petición HTTP
@Component
// @RequiredArgsConstructor genera el constructor con los campos final
// Equivale a @AllArgsConstructor pero solo para campos final
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain   // la cadena de filtros siguiente
    ) throws ServletException, IOException {

        // Extraemos el header Authorization
        final String authHeader = request.getHeader("Authorization");

        // Si no hay token o no empieza con "Bearer ", continuamos sin autenticar
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extraemos el token (quitamos "Bearer ")
        final String jwt = authHeader.substring(7);
        final String userEmail;

        try {
            userEmail = jwtService.extractUsername(jwt);
        } catch (Exception e) {
            // Token malformado — continuamos sin autenticar
            filterChain.doFilter(request, response);
            return;
        }

        // Solo procesamos si hay email Y el usuario no está ya autenticado
        // SecurityContextHolder guarda la autenticación actual de la petición
        if (userEmail != null
                && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

            if (jwtService.isTokenValid(jwt, userDetails)) {
                // Creamos el token de autenticación de Spring Security
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,                          // credentials (no necesarias post-auth)
                                userDetails.getAuthorities()   // roles/permisos
                        );

                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // Registramos la autenticación en el contexto de la petición
                // A partir de aquí, Spring Security sabe que está autenticado
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Pasamos la petición al siguiente filtro
        filterChain.doFilter(request, response);
    }
}
