package com.dsabg.dsabgauthservice.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupOtpRequest {

    private UserRegistrationRequest user;
    private String otp;
}