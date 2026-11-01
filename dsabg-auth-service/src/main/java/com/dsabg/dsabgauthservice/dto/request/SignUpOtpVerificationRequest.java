package com.dsabg.dsabgauthservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SignUpOtpVerificationRequest {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String otp;
}
