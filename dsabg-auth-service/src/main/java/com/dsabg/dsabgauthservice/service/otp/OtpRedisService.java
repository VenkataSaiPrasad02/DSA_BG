
package com.dsabg.dsabgauthservice.service.otp;

public interface OtpRedisService {

    void saveOtp(String email, String otp);

    String getOtp(String email);

    void deleteOtp(String email);

    void savePasswordResetOtp(String email, String otp);

    String getPasswordResetOtp(String email);

    void deletePasswordResetOtp(String email);
    void saveLoginOtp(String email, String otp);

    String getLoginOtp(String email);

    void deleteLoginOtp(String email);
}
