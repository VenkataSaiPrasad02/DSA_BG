
package com.dsabg.dsabgauthservice.controller;

import com.dsabg.dsabgauthservice.dto.request.*;
import com.dsabg.dsabgauthservice.dto.response.AuthResponse;
import com.dsabg.dsabgauthservice.service.apiResponse.API_Response;
import com.dsabg.dsabgauthservice.service.authService.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<API_Response<String>> signup(
            @Valid @RequestBody UserRegistrationRequest user) {

        authService.SignUp(user);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new API_Response<>(
                        "Verification OTP sent to your email", null));
    }

    @PostMapping("/signup/verify-otp")
    public ResponseEntity<API_Response<String>> verifySignupOtp(
            @RequestBody SignupOtpRequest request) {

        authService.verifyRegistrationOtp(
                request.getUser(),
                request.getOtp());

        return ResponseEntity.ok(
                new API_Response<>(
                        "Email verified and registration successful",
                        null));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<API_Response<String>> forgotPassword(
            @RequestBody ForgotPasswordRequest request) {

        authService.forgotPassword(request.getEmail());

        return ResponseEntity.ok(
                new API_Response<>(
                        "If the account is eligible, password reset instructions will be sent",
                        null));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<API_Response<String>> resetPassword(
            @RequestBody ResetPasswordRequest request) {

        authService.resetPassword(request);

        return ResponseEntity.ok(
                new API_Response<>("Password reset successful", null));
    }
    @PostMapping("/login")
    public ResponseEntity<API_Response<String>> login(
            @Valid @RequestBody UserLoginRequest request) {

        authService.login(request);

        return ResponseEntity.ok(
                new API_Response<>(
                        "Login OTP sent to your registered email",
                        null));
    }

    @PostMapping("/login/verify-otp")
    public ResponseEntity<AuthResponse> verifyLoginOtp(
            @RequestBody LoginOtpRequest request) {

        String token = authService.verifyLoginOtp(request);

        AuthResponse response = new AuthResponse(
                "Login successful",
                token,
                "Bearer"
        );

        return ResponseEntity.ok(response);
    }
}
