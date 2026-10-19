
package com.dsabg.dsabgauthservice.service.authService.authServiceImpl;

import com.dsabg.dsabgauthservice.dto.request.LoginOtpRequest;
import com.dsabg.dsabgauthservice.dto.request.ResetPasswordRequest;
import com.dsabg.dsabgauthservice.dto.request.UserLoginRequest;
import com.dsabg.dsabgauthservice.dto.request.UserRegistrationRequest;
import com.dsabg.dsabgauthservice.entity.Users;
import com.dsabg.dsabgauthservice.exception.DuplicateResourceException;
import com.dsabg.dsabgauthservice.repository.UserRepository;
import com.dsabg.dsabgauthservice.service.authService.AuthService;
import com.dsabg.dsabgauthservice.service.jwt.JwtService;
import com.dsabg.dsabgauthservice.service.otp.OtpEmailService;
import com.dsabg.dsabgauthservice.service.otp.OtpGeneratorService;
import com.dsabg.dsabgauthservice.service.otp.OtpRedisService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository repo;
    private final JwtService jwtService;
    private final OtpGeneratorService otpGeneratorService;
    private final OtpRedisService otpRedisService;
    private final OtpEmailService otpEmailService;

    @Override
    @Transactional
    public void SignUp(UserRegistrationRequest user) {

        String email = user.getEmail().trim()
                .toLowerCase(java.util.Locale.ROOT);

        if (repo.existsByEmail(email)) {
            throw new DuplicateResourceException(
                    "User with email already exists!");
        }
        String otp = otpGeneratorService.generateOtp();
        otpRedisService.saveOtp(email, otp);
        otpEmailService.sendOtp(email, otp);
    }


    @Override
    public void login(UserLoginRequest user) {

        String login = user.getLogin().trim();

        Users account;

        if (login.contains("@")) {
            account = repo.findByEmail(
                            login.toLowerCase(java.util.Locale.ROOT))
                    .orElseThrow(() ->
                            new org.springframework.security.authentication
                                    .BadCredentialsException(
                                    "Invalid username/email or password"));
        } else {
            account = repo.findByUsername(login)
                    .orElseThrow(() ->
                            new org.springframework.security.authentication
                                    .BadCredentialsException(
                                    "Invalid username/email or password"));
        }

        if (!passwordEncoder.matches(
                user.getPassword(), account.getPassword())) {
            throw new org.springframework.security.authentication
                    .BadCredentialsException(
                    "Invalid username/email or password");
        }

        if (!"ACTIVE".equals(account.getStatus())) {
            throw new org.springframework.security.authentication
                    .BadCredentialsException("Account is not active");
        }

        String email = account.getEmail()
                .trim().toLowerCase(java.util.Locale.ROOT);

        String otp = otpGeneratorService.generateOtp();

        otpRedisService.saveLoginOtp(email, otp);

        try {
            otpEmailService.sendOtp(email, otp);
        } catch (RuntimeException ex) {
            otpRedisService.deleteLoginOtp(email);
            throw ex;
        }
    }
    @Override
    @Transactional
    public void verifyRegistrationOtp(
            UserRegistrationRequest user,
            String otp) {

        String email = user.getEmail().trim()
                .toLowerCase(java.util.Locale.ROOT);

        if (repo.existsByEmail(email)) {
            throw new DuplicateResourceException(
                    "User with email already exists!");
        }

        String storedOtp = otpRedisService.getOtp(email);

        if (storedOtp == null || !storedOtp.equals(otp)) {
            throw new org.springframework.security.authentication
                    .BadCredentialsException(
                    "Invalid or expired OTP");
        }


        otpRedisService.deleteOtp(email);

        Users users = new Users();

        Long maxId = repo.getMaxIdNumber();
        long nextId = (maxId == null) ? 1 : maxId + 1;

        users.setId("UU" + String.format("%03d", nextId));
        users.setUsername(user.getUsername());
        users.setPassword(passwordEncoder.encode(user.getPassword()));
        users.setEmail(email);
        users.setPhone(user.getPhone());
        users.setStatus("ACTIVE");

        repo.save(users);
    }
    @Override
    public void forgotPassword(String email) {

        String normalizedEmail = email.trim()
                .toLowerCase(java.util.Locale.ROOT);

        Users account = repo.findByEmail(normalizedEmail)
                .orElseThrow(() ->
                        new org.springframework.security.authentication
                                .BadCredentialsException(
                                "Unable to process password reset"));

        String otp = otpGeneratorService.generateOtp();

        otpRedisService.savePasswordResetOtp(
                normalizedEmail, otp);

        try {
            otpEmailService.sendOtp(normalizedEmail, otp);
        } catch (RuntimeException ex) {
            otpRedisService.deletePasswordResetOtp(normalizedEmail);
            throw ex;
        }
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {

        String email = request.getEmail().trim()
                .toLowerCase(java.util.Locale.ROOT);

        String storedOtp = otpRedisService.getPasswordResetOtp(email);

        if (storedOtp == null ||
                !storedOtp.equals(request.getOtp())) {
            throw new org.springframework.security.authentication
                    .BadCredentialsException(
                    "Invalid or expired OTP");
        }

        Users account = repo.findByEmail(email)
                .orElseThrow(() ->
                        new org.springframework.security.authentication
                                .BadCredentialsException(
                                "Unable to process password reset"));

        account.setPassword(
                passwordEncoder.encode(request.getNewPassword()));

        repo.save(account);

        otpRedisService.deletePasswordResetOtp(email);
    }
    @Override
    public String verifyLoginOtp(LoginOtpRequest request) {

        String email = request.getEmail()
                .trim().toLowerCase(java.util.Locale.ROOT);

        String storedOtp = otpRedisService.getLoginOtp(email);

        if (storedOtp == null ||
                !storedOtp.equals(request.getOtp())) {
            throw new org.springframework.security.authentication
                    .BadCredentialsException(
                    "Invalid or expired OTP");
        }

        Users account = repo.findByEmail(email)
                .orElseThrow(() ->
                        new org.springframework.security.authentication
                                .BadCredentialsException(
                                "Invalid or expired OTP"));

        if (!"ACTIVE".equals(account.getStatus())) {
            throw new org.springframework.security.authentication
                    .BadCredentialsException("Account is not active");
        }

        otpRedisService.deleteLoginOtp(email);

        return jwtService.generateToken(
                account.getId(), account.getEmail());
    }
}
