package com.dsabg.dsabgauthservice.dto.request;

import jakarta.validation.constraints.NotBlank;

public record OtpEmailMessage (
    String email,
    String otp
){}
