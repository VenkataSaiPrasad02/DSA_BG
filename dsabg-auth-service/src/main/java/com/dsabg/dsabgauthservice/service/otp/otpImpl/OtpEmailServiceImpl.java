
package com.dsabg.dsabgauthservice.service.otp.otpImpl;

import com.dsabg.dsabgauthservice.service.otp.OtpEmailProducer;
import com.dsabg.dsabgauthservice.service.otp.OtpEmailService;
import org.springframework.stereotype.Service;

@Service
public class OtpEmailServiceImpl implements OtpEmailService {

    private final OtpEmailProducer otpEmailProducer;

    public OtpEmailServiceImpl(OtpEmailProducer otpEmailProducer) {
        this.otpEmailProducer = otpEmailProducer;
    }

    @Override
    public void sendOtp(String email, String otp) {
        otpEmailProducer.publishOtpEmail(email, otp);
    }
}
