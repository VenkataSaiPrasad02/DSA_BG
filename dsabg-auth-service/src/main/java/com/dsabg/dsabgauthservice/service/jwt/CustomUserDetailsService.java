package com.dsabg.dsabgauthservice.service.jwt;

import com.dsabg.dsabgauthservice.entity.Users;
import com.dsabg.dsabgauthservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService
        implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String login) {

        Users user = login.contains("@")
                ? userRepository.findByEmail(login)
                .orElseThrow(() ->
                             new UsernameNotFoundException("User not found"))
                : userRepository.findByUsername(login)
                .orElseThrow(() ->
                             new UsernameNotFoundException("User not found"));

        return new CustomUserDetails(user);
    }
}
