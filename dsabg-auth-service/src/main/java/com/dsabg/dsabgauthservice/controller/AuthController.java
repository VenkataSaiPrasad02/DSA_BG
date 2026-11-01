package com.dsabg.dsabgauthservice.controller;

import com.dsabg.dsabgauthservice.dto.request.ForgotPasswordRequest;
import com.dsabg.dsabgauthservice.dto.request.LoginOtpRequest;
import com.dsabg.dsabgauthservice.dto.request.LoginRequest;
import com.dsabg.dsabgauthservice.dto.request.ResetPasswordRequest;
import com.dsabg.dsabgauthservice.dto.request.SignUpOtpVerificationRequest;
import com.dsabg.dsabgauthservice.dto.request.SignUpRequest;
import com.dsabg.dsabgauthservice.dto.response.AuthResponse;
import com.dsabg.dsabgauthservice.dto.response.AuthUserResponse;
import com.dsabg.dsabgauthservice.repository.UserRepository;
import com.dsabg.dsabgauthservice.service.apiResponse.API_Response;
import com.dsabg.dsabgauthservice.service.authService.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private static final Logger log =
            LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final UserRepository userRepository;

    @PostMapping("/signUp")
    public ResponseEntity<API_Response<String>> signUp(
            @Valid @RequestBody SignUpRequest signUpRequest) {

        authService.signUp(signUpRequest);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new API_Response<>(
                        "Verification OTP sent to your email", null));
    }

    @PostMapping("/signUp/verify-otp")
    public ResponseEntity<API_Response<String>> signUpOtpVerification(
            @Valid @RequestBody SignUpOtpVerificationRequest request) {

        log.info("SignUp OTP verification endpoint reached");

        authService.signUpOtpVerification(request.getEmail(), request.getOtp());

        log.info("SignUp OTP verification completed");

        return ResponseEntity.ok(
                new API_Response<>(
                        "Email verified and registration successful", null));
    }

    @PostMapping("/login")
    public ResponseEntity<API_Response<String>> login(
            @Valid @RequestBody LoginRequest loginRequest) {

        authService.login(loginRequest);

        return ResponseEntity.ok(
                new API_Response<>(
                        "Login OTP sent to your registered email", null));
    }

    @PostMapping("/login/verify-otp")
    public ResponseEntity<AuthResponse> loginOtpVerification(
            @Valid @RequestBody LoginOtpRequest request) {

        String token = authService.loginOtpVerification(request);

        AuthResponse response = new AuthResponse(
                "Login successful", token, "Bearer");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<AuthUserResponse> getUserById(
            @PathVariable String userId,
            Authentication authentication) {

        if (authentication == null
                || authentication.getName() == null
                || !authentication.getName().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return userRepository.findById(userId)
                .map(user -> ResponseEntity.ok(new AuthUserResponse(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail())))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<API_Response<String>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        authService.forgotPassword(request.getEmail());

        return ResponseEntity.ok(
                new API_Response<>(
                        "If the account is eligible, password reset instructions will be sent",
                        null));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<API_Response<String>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        authService.resetPassword(request);

        return ResponseEntity.ok(
                new API_Response<>("Password reset successful", null));
    }
}
