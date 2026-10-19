package com.dsabg.dsabgauthservice.service.otp.otpImpl;

import com.dsabg.dsabgauthservice.service.otp.OtpGeneratorService;

import java.security.SecureRandom;

public class OtpGeneratorServiceImpl implements OtpGeneratorService {
    private static final int OTP_BOUND = 1_000_000;

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String generateOtp() {
        int otp = secureRandom.nextInt(OTP_BOUND);
        return String.format("%06d", otp);
    }
}
