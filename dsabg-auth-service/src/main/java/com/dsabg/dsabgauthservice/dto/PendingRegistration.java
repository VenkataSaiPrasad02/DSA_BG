package com.dsabg.dsabgauthservice.dto;

public record PendingRegistration(
        String username,
        String email,
        String phone,
        String hashedPassword
) {}
