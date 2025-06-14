package com.monitoring.server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuración de WebSockets para actualizaciones en tiempo real
 * Permite enviar métricas del sistema automáticamente a los clientes conectados
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Habilitar broker simple en memoria para topics
        config.enableSimpleBroker("/topic");
        
        // Prefijo para mensajes de aplicación
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint para conectar WebSocket desde el frontend
        registry.addEndpoint("/ws-metrics")
                .setAllowedOriginPatterns("*") // En producción, especificar dominios exactos
                .withSockJS(); // Fallback para navegadores que no soporten WebSocket nativo
    }
}