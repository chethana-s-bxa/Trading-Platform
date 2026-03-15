package com.trading.tradingplatform.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuration class for enabling WebSocket messaging in the application.
 *
 * This configuration sets up:
 * - STOMP messaging protocol
 * - Message broker for topic broadcasting
 * - WebSocket connection endpoint
 *
 * WebSocket allows the server to push real-time updates to clients
 * without requiring the client to repeatedly request data.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Registers the WebSocket endpoint that clients will use
     * to establish a connection with the server.
     *
     * SockJS is enabled to provide fallback options for browsers
     * that do not support native WebSocket.
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-market")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    /**
     * Configures the message broker used for broadcasting messages
     * to subscribed clients.
     *
     * /topic  -> used for broadcasting messages
     * /app    -> used for client-to-server messaging
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {

        registry.enableSimpleBroker("/topic");

        registry.setApplicationDestinationPrefixes("/app");
    }
}