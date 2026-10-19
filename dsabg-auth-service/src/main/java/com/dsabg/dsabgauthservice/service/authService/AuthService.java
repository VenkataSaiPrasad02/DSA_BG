package com.dsabg.dsabgauthservice.service.authService;

import com.dsabg.dsabgauthservice.dto.request.LoginOtpRequest;
import com.dsabg.dsabgauthservice.dto.request.ResetPasswordRequest;
import com.dsabg.dsabgauthservice.dto.request.UserLoginRequest;
import com.dsabg.dsabgauthservice.dto.request.UserRegistrationRequest;
import jakarta.validation.Valid;

public interface AuthService {
    void SignUp(@Valid UserRegistrationRequest userRegistrationRequest);

    void login(@Valid UserLoginRequest user);
    void verifyRegistrationOtp(
            UserRegistrationRequest user,
            String otp
    );
    void forgotPassword(String email);

    void resetPassword(ResetPasswordRequest request);


    String verifyLoginOtp(LoginOtpRequest request);
}
