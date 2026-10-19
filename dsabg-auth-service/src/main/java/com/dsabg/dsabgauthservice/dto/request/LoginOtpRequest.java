package com.dsabg.dsabgauthservice.dto.request;

import lombok.Data;

@Data
public class LoginOtpRequest {
    private String email;
    private String otp;
}