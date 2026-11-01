//package com.dsabg.dsabgauthservice.config;
//
//import com.dsabg.dsabgauthservice.service.jwt.JwtService;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
//import org.springframework.security.oauth2.core.user.OAuth2User;
//import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
//import org.springframework.stereotype.Component;
//import org.springframework.web.util.UriComponentsBuilder;
//
//import java.io.IOException;
//import java.time.Instant;
//import java.util.LinkedHashMap;
//import java.util.Map;
//
//@Component
//@RequiredArgsConstructor
//public class OAuth2AuthenticationSuccessHandler
//        implements AuthenticationSuccessHandler {
//
//    private final JwtService jwtService;
//
//    @Override
//    public void onAuthenticationSuccess(
//            HttpServletRequest request,
//            HttpServletResponse response,
//            Authentication authentication) throws IOException {
//
//        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
//
//        String provider = authentication instanceof OAuth2AuthenticationToken token
//                ? token.getAuthorizedClientRegistrationId()
//                : "unknown";
//
//        // Log provider attributes safely (convert Instant to String)
//        Map<String, Object> safeAttributes = new LinkedHashMap<>();
//        oauthUser.getAttributes().forEach((key, value) -> {
//            if (value instanceof Instant instant) {
//                safeAttributes.put(key, instant.toString());
//            } else {
//                safeAttributes.put(key, value);
//            }
//        });
//
//        System.out.println("\n========== OAUTH2 USER DETAILS ==========");
//        System.out.println("Provider  : " + provider);
//        System.out.println("Attributes: " + safeAttributes);
//        System.out.println("=========================================\n");
//
//        // Google uses "sub"; fallback to "id" then getName()
//        String userId = oauthUser.getAttribute("sub");
//        if (userId == null) {
//            userId = String.valueOf(oauthUser.getAttribute("id"));
//        }
//        if ("null".equals(userId)) {
//            userId = oauthUser.getName();
//        }
//
//        String email = oauthUser.getAttribute("email");
//        if (email == null || email.isBlank()) {
//            email = userId + "@" + provider + ".oauth.local";
//        }
//
//        String token = jwtService.generateToken(userId, username);
//
//        String targetUrl = UriComponentsBuilder
//                .fromUriString("http://localhost:5173/oauth2/redirect")
//                .queryParam("token", token)
//                .build()
//                .encode()
//                .toUriString();
//
//        response.sendRedirect(targetUrl);
//    }
//}
