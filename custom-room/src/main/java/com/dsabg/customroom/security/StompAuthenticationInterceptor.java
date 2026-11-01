
package com.dsabg.customroom.security;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.security.Principal;

@Component
@RequiredArgsConstructor
@Slf4j
public class StompAuthenticationInterceptor implements ChannelInterceptor {
    private final JwtFilter jwtFilter;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String header = accessor.getFirstNativeHeader(HttpHeaders.AUTHORIZATION);
            if (header == null || !header.startsWith("Bearer ")) {
                throw new MessageDeliveryException(message, "Authentication Required");
            }
            String authToken = header.substring(7);
            if ( authToken.isEmpty()  ) {
                throw new MessageDeliveryException(message, "Missing JWT Token");
            }
            try {
                String userId = jwtFilter.verifyToken(authToken);
                accessor.setUser(new Principal() {
                    @Override
                    public String getName() {
                        return userId;
                    }
                });
                log.info("Authenticated STOMP CONNECT principal userId={}", userId);
            } catch (io.jsonwebtoken.JwtException |
                     IllegalArgumentException ex) {
                throw new MessageDeliveryException(
                        message, "Invalid STOMP authentication token");
            }
        } else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            Principal principal = accessor.getUser();
            log.info(
                    "STOMP subscription userId={} destination={}",
                    principal == null ? null : principal.getName(),
                    accessor.getDestination()
            );
        }
        return message;
    }
}
