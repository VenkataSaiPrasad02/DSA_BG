package com.dsabg.dsabgauthservice.service.otp.otpImpl;

import com.dsabg.dsabgauthservice.service.otp.OtpRedisService;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.Locale;

public class OtpRedisServiceImpl implements OtpRedisService {
    private static final Duration OTP_TTL = Duration.ofMinutes(2);
    private static final String OTP_KEY_PREFIX = "dsabg:signup:otp:";

    private final StringRedisTemplate redisTemplate;

    public OtpRedisServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void saveOtp(String email, String otp) {
        redisTemplate.opsForValue().set(
                createOtpKey(email),
                otp,
                OTP_TTL
        );
    }

    @Override
    public String getOtp(String email) {
        return redisTemplate.opsForValue().get(createOtpKey(email));
    }

    @Override
    public void deleteOtp(String email) {
        redisTemplate.delete(createOtpKey(email));
    }

    private String createOtpKey(String email) {
        return OTP_KEY_PREFIX + email.trim().toLowerCase(Locale.ROOT);
    }
    private static final String RESET_KEY_PREFIX =
            "dsabg:password-reset:otp:";

    @Override
    public void savePasswordResetOtp(String email, String otp) {
        redisTemplate.opsForValue().set(
                RESET_KEY_PREFIX + normalizeEmail(email),
                otp,
                Duration.ofMinutes(3)
        );
    }

    @Override
    public String getPasswordResetOtp(String email) {
        return redisTemplate.opsForValue().get(
                RESET_KEY_PREFIX + normalizeEmail(email)
        );
    }

    @Override
    public void deletePasswordResetOtp(String email) {
        redisTemplate.delete(
                RESET_KEY_PREFIX + normalizeEmail(email)
        );
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
    private static final String LOGIN_KEY_PREFIX =
            "dsabg:login:otp:";

    @Override
    public void saveLoginOtp(String email, String otp) {
        redisTemplate.opsForValue().set(
                LOGIN_KEY_PREFIX + email.trim().toLowerCase(Locale.ROOT),
                otp,
                OTP_TTL
        );
    }

    @Override
    public String getLoginOtp(String email) {
        return redisTemplate.opsForValue().get(
                LOGIN_KEY_PREFIX + email.trim().toLowerCase(Locale.ROOT)
        );
    }

    @Override
    public void deleteLoginOtp(String email) {
        redisTemplate.delete(
                LOGIN_KEY_PREFIX + email.trim().toLowerCase(Locale.ROOT)
        );
    }
}
