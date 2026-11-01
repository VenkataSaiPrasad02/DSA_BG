package com.dsabg.dsabgauthservice.service.otp.otpImpl;

import com.dsabg.dsabgauthservice.dto.PendingRegistration;
import com.dsabg.dsabgauthservice.service.otp.OtpRedisService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Locale;
import java.util.Map;
@Service
public class OtpRedisServiceImpl implements OtpRedisService {
    private static final Duration OTP_TTL = Duration.ofMinutes(2);
    private static final String OTP_KEY_PREFIX = "dsabg:signup:otp:";
    private static final String PENDING_KEY_PREFIX =
            "dsabg:signup:pending:";
    private static final Duration PENDING_TTL = Duration.ofMinutes(5);

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

    @Override
    public void savePendingRegistration(
            String email, PendingRegistration pending) {

        String key = PENDING_KEY_PREFIX + normalizeEmail(email);

        Map<String, String> values = Map.of(
                "username", pending.username(),
                "email", pending.email(),
                "phone", pending.phone(),
                "hashedPassword", pending.hashedPassword());

        redisTemplate.opsForHash().putAll(key, values);
        redisTemplate.expire(key, PENDING_TTL);
    }

    @Override
    public PendingRegistration getPendingRegistration(String email) {
        String key = PENDING_KEY_PREFIX + normalizeEmail(email);

        Map<Object, Object> values =
                redisTemplate.opsForHash().entries(key);

        if (values.isEmpty()) {
            return null;
        }

        return new PendingRegistration(
                (String) values.get("username"),
                (String) values.get("email"),
                (String) values.get("phone"),
                (String) values.get("hashedPassword"));
    }

    @Override
    public void deletePendingRegistration(String email) {
        redisTemplate.delete(PENDING_KEY_PREFIX + normalizeEmail(email));
    }
}
