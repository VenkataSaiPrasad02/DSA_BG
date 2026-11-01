package com.dsabg.dsabgauthservice.service.authService.authServiceImpl;

import com.dsabg.dsabgauthservice.dto.PendingRegistration;
import com.dsabg.dsabgauthservice.dto.request.LoginOtpRequest;
import com.dsabg.dsabgauthservice.dto.request.LoginRequest;
import com.dsabg.dsabgauthservice.dto.request.ResetPasswordRequest;
import com.dsabg.dsabgauthservice.dto.request.SignUpRequest;
import com.dsabg.dsabgauthservice.entity.Enum.Roles;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import com.dsabg.dsabgauthservice.config.RabbitMQConfig;
import com.dsabg.dsabgauthservice.event.UserRegisteredEvent;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final Logger log =
            LoggerFactory.getLogger(AuthServiceImpl.class);

    private final PasswordEncoder passwordEncoder;
    private final UserRepository repo;
    private final JwtService jwtService;
    private final OtpGeneratorService otpGeneratorService;
    private final OtpRedisService otpRedisService;
    private final OtpEmailService otpEmailService;
    private final RabbitTemplate rabbitTemplate;

    @Override
    public void signUp(SignUpRequest signUpRequest) {

        String username = signUpRequest.getUsername().trim();
        String email = normalizeEmail(signUpRequest.getEmail());


        if (repo.existsByUsername(username)) {
            throw new DuplicateResourceException(
                    "Username already exists!");
        }

        String otp = otpGeneratorService.generateOtp();

        PendingRegistration pending = new PendingRegistration(
                username,
                email,
                signUpRequest.getPhone().trim(),
                passwordEncoder.encode(signUpRequest.getPassword())
        );

        otpRedisService.savePendingRegistration(email, pending);
        otpRedisService.saveOtp(email, otp);

        try {
            otpEmailService.sendOtp(email, otp);
            log.info("SignUp OTP sent successfully");
        } catch (RuntimeException ex) {
            otpRedisService.deleteOtp(email);
            otpRedisService.deletePendingRegistration(email);
            log.error("Failed to send signUp OTP", ex);
            throw ex;
        }
    }

    @Override
    @Transactional
    public void signUpOtpVerification(String email, String otp) {
        String normalizedEmail = normalizeEmail(email);

        if (repo.existsByEmail(normalizedEmail)) {
            throw new DuplicateResourceException(
                    "User with email already exists!");
        }

        PendingRegistration pending =
                otpRedisService.getPendingRegistration(normalizedEmail);

        if (pending == null) {
            throw new BadCredentialsException(
                    "SignUp session expired, please sign up again");
        }

        String storedOtp = otpRedisService.getOtp(normalizedEmail);

        if (storedOtp == null || !storedOtp.equals(otp)) {
            throw new BadCredentialsException(
                    "Invalid or expired OTP");
        }

        Users account = new Users();

        Long maxId = repo.getMaxIdNumber();
        long nextId = (maxId == null) ? 1 : maxId + 1;

        account.setId("UU" + String.format("%03d", nextId));
        account.setUsername(pending.username());
        account.setPassword(pending.hashedPassword());
        account.setEmail(normalizedEmail);
        account.setPhone(pending.phone());
        account.setStatus("ACTIVE");

        repo.save(account);


        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        rabbitTemplate.convertAndSend(
                                RabbitMQConfig.USER_REGISTERED_EXCHANGE,
                                RabbitMQConfig.USER_REGISTERED_ROUTING_KEY,
                                new UserRegisteredEvent(
                                        account.getId(),
                                        account.getUsername()
                                )
                        );
                    }
                }
        );


        otpRedisService.deleteOtp(normalizedEmail);
        otpRedisService.deletePendingRegistration(normalizedEmail);

        log.info("SignUp OTP verified and registration completed");
    }

    @Override
    public void login(LoginRequest loginRequest) {
        String loginField = loginRequest.getLogin().trim();

        Users account;

        if (loginField.contains("@")) {
            account = repo.findByEmail(normalizeEmail(loginField))
                    .orElseThrow(() ->
                            new BadCredentialsException(
                                    "Invalid username/email or password"));
        } else {
            account = repo.findByUsername(loginField)
                    .orElseThrow(() ->
                            new BadCredentialsException(
                                    "Invalid username/email or password"));
        }

        if (!passwordEncoder.matches(
                loginRequest.getPassword(), account.getPassword())) {
            throw new BadCredentialsException(
                    "Invalid username/email or password");
        }

        if (!"ACTIVE".equals(account.getStatus())) {
            throw new BadCredentialsException("Account is not active");
        }

        String email = normalizeEmail(account.getEmail());
        String otp = otpGeneratorService.generateOtp();

        otpRedisService.saveLoginOtp(email, otp);

        try {
            otpEmailService.sendOtp(email, otp);
            log.info("Login OTP sent successfully");
        } catch (RuntimeException ex) {
            otpRedisService.deleteLoginOtp(email);
            log.error("Failed to send login OTP", ex);
            throw ex;
        }
    }

    @Override
    public String loginOtpVerification(LoginOtpRequest request) {
        String email = normalizeEmail(request.getEmail());

        String storedOtp = otpRedisService.getLoginOtp(email);

        if (storedOtp == null || !storedOtp.equals(request.getOtp())) {
            throw new BadCredentialsException(
                    "Invalid or expired OTP");
        }

        Users account = repo.findByEmail(email)
                .orElseThrow(() ->
                        new BadCredentialsException(
                                "Invalid or expired OTP"));

        if (!"ACTIVE".equals(account.getStatus())) {
            throw new BadCredentialsException("Account is not active");
        }

        String token = jwtService.generateToken(
                account.getId(), account.getUsername(), account.getEmail(), Roles.USER);

        otpRedisService.deleteLoginOtp(email);

        log.info("Login OTP verified successfully");

        return token;
    }

    @Override
    public void forgotPassword(String email) {
        String normalizedEmail = normalizeEmail(email);

        Users account = repo.findByEmail(normalizedEmail)
                .orElseThrow(() ->
                        new BadCredentialsException(
                                "Unable to process password reset"));

        String otp = otpGeneratorService.generateOtp();

        otpRedisService.savePasswordResetOtp(normalizedEmail, otp);

        try {
            otpEmailService.sendOtp(normalizedEmail, otp);
            log.info("Password reset OTP sent successfully");
        } catch (RuntimeException ex) {
            otpRedisService.deletePasswordResetOtp(normalizedEmail);
            log.error("Failed to send password reset OTP", ex);
            throw ex;
        }
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String email = normalizeEmail(request.getEmail());

        String storedOtp = otpRedisService.getPasswordResetOtp(email);

        if (storedOtp == null || !storedOtp.equals(request.getOtp())) {
            throw new BadCredentialsException("Invalid or expired OTP");
        }

        Users account = repo.findByEmail(email)
                .orElseThrow(() ->
                        new BadCredentialsException(
                                "Unable to process password reset"));

        account.setPassword(
                passwordEncoder.encode(request.getNewPassword()));

        repo.save(account);

        otpRedisService.deletePasswordResetOtp(email);

        log.info("Password reset completed successfully");
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email must not be blank");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
