package com.dsabg.dsabgauthservice.service.authService;

import com.dsabg.dsabgauthservice.dto.request.LoginOtpRequest;
import com.dsabg.dsabgauthservice.dto.request.LoginRequest;
import com.dsabg.dsabgauthservice.dto.request.ResetPasswordRequest;
import com.dsabg.dsabgauthservice.dto.request.SignUpOtpVerificationRequest;
import com.dsabg.dsabgauthservice.dto.request.SignUpRequest;
import jakarta.validation.Valid;

public interface AuthService {

    void signUp(@Valid SignUpRequest signUpRequest);

    void signUpOtpVerification(String email, String otp);

    void login(@Valid LoginRequest loginRequest);

    String loginOtpVerification(LoginOtpRequest request);

    void forgotPassword(String email);

    void resetPassword(ResetPasswordRequest request);
}
