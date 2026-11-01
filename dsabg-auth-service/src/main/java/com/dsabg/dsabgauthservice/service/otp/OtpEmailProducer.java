package com.dsabg.dsabgauthservice.service.otp;

public interface OtpEmailProducer {
    void publishOtpEmail(String email, String otp);
}
