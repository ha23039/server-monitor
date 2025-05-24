package com.monitoring.server.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${auth0.domain}")
    private String auth0Domain;

    @Value("${auth0.audience}")
    private String auth0Audience;

    @Bean
    @Order(1) // Mayor prioridad - se ejecuta primero
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/**") // Solo aplica a rutas que empiecen con /api/
            .csrf(csrf -> csrf.disable()) // Deshabilitamos CSRF para APIs
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Sin sesiones para APIs
            .oauth2ResourceServer(oauth2 -> 
                oauth2.jwt(jwt -> jwt
                    .decoder(jwtDecoder())
                    .jwtAuthenticationConverter(jwtAuthenticationConverter()))) // Convertir claims de Auth0
            .authorizeHttpRequests(auth -> auth
                // Rutas públicas de la API
                .requestMatchers("/api/public/**", "/api/health/**").permitAll()
                // Rutas solo para administradores (usando roles de Auth0)
                .requestMatchers("/api/admin/**").hasAuthority("ROLE_admin")
                // Rutas para usuarios autenticados
                .requestMatchers("/api/user/**").hasAnyAuthority("ROLE_user", "ROLE_admin")
                // Todas las demás rutas de API requieren autenticación
                .anyRequest().authenticated()
            );
        
        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        String jwkSetUri = "https://" + auth0Domain + "/.well-known/jwks.json";
        return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            // Extraer roles de Auth0 claims usando el namespace personalizado
            Collection<String> roles = extractRoles(jwt.getClaims());
            
            return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
        });
        return converter;
    }

    @SuppressWarnings("unchecked")
    private Collection<String> extractRoles(Map<String, Object> claims) {
        // Auth0 custom claim namespace
        String rolesKey = "https://servermonitor.api/roles";
        
        Object rolesObj = claims.get(rolesKey);
        if (rolesObj instanceof Collection) {
            return (Collection<String>) rolesObj;
        } else if (rolesObj instanceof String) {
            return List.of((String) rolesObj);
        }
        
        // Fallback: buscar en otros claims comunes
        Object authoritiesObj = claims.get("authorities");
        if (authoritiesObj instanceof Collection) {
            return (Collection<String>) authoritiesObj;
        }
        
        // Si no encuentra roles, devolver lista vacía
        return new ArrayList<>();
    }
}