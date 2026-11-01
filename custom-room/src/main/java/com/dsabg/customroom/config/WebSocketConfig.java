
package com.dsabg.customroom.config;

import com.dsabg.customroom.security.StompAuthenticationInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
private final StompAuthenticationInterceptor stompAuthenticationInterceptor;
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {

        // Destinations used for server-to-client messages
        registry.enableSimpleBroker("/topic", "/queue");

        // Destinations used for messages sent from clients to the server
        registry.setApplicationDestinationPrefixes("/app");

        // Private destinations for individual authenticated users
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {

        // WebSocket connection endpoint
        registry.addEndpoint("/ws")
                .setAllowedOrigins("http://localhost:5173");
    }
    @Override
    public void configureClientInboundChannel(
            ChannelRegistration registration
    ){
        registration.interceptors(stompAuthenticationInterceptor);
    }
}
