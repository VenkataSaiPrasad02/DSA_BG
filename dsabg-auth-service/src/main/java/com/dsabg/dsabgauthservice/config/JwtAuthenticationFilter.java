//
//package com.dsabg.dsabgauthservice.config;
//
//import com.dsabg.dsabgauthservice.service.jwt.JwtService;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//
//@Component
//public class JwtAuthenticationFilter extends OncePerRequestFilter {
//
//    private final JwtService jwtService;
//
//    public JwtAuthenticationFilter(JwtService jwtService) {
//        this.jwtService = jwtService;
//    }
//
//    @Override
//    protected void doFilterInternal(
//            HttpServletRequest request,
//            HttpServletResponse response,
//            FilterChain filterChain
//    ) throws ServletException, IOException {
//        String authHeader = request.getHeader("Authorization");
//        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//            filterChain.doFilter(request, response);
//            return;
//        }
//        String token = authHeader.substring(7);
//        try {
//            String userId = jwtService.extractUserId(token);
//            if (userId != null
//                    && SecurityContextHolder.getContext().getAuthentication() == null
//                    && jwtService.isTokenValid(token)) {
//                UsernamePasswordAuthenticationToken authentication =
//                        new UsernamePasswordAuthenticationToken(
//                                userId,
//                                null,
//                                java.util.Collections.emptyList()
//                        );
//                authentication.setDetails(
//                        new WebAuthenticationDetailsSource()
//                                .buildDetails(request)
//                );
//                SecurityContextHolder.getContext()
//                        .setAuthentication(authentication);
//            }
//        } catch (RuntimeException exception) {
//            SecurityContextHolder.clearContext();
//        }
//        filterChain.doFilter(request, response);
//    }
//}
