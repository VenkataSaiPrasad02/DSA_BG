
package com.dsabg.dsabgauthservice.service.otp.otpImpl;

import com.dsabg.dsabgauthservice.config.RabbitMQConfig;
import com.dsabg.dsabgauthservice.dto.request.OtpEmailMessage;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class OtpEmailConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(OtpEmailConsumer.class);

    private final JavaMailSender mailSender;

    public OtpEmailConsumer(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @RabbitListener(queues = RabbitMQConfig.EMAIL_QUEUE_NAME)
    public void consumeOtpEmail(OtpEmailMessage message) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(message.email());
            helper.setSubject("⚔ DSABG | Email Verification");
            helper.setText(buildHtml(message.otp()), true);

            mailSender.send(mimeMessage);

            log.info("OTP email sent successfully to {}", message.email());

        } catch (MessagingException ex) {
            throw new IllegalStateException(
                    "Failed to prepare DSABG verification email", ex);
        }
    }

    private String buildHtml(String otp) {
        return """
            <!DOCTYPE html>
            <html>
            <body style="margin:0;padding:0;background:#080b14;
                         font-family:Arial,Helvetica,sans-serif;color:#ffffff;">
              <div style="max-width:560px;margin:30px auto;padding:0 16px;">
                <div style="background:#101729;border:1px solid #263451;
                            border-radius:16px;overflow:hidden;">

                  <div style="padding:30px 24px;text-align:center;
                              background:linear-gradient(135deg,#111c38,#25134a);
                              border-bottom:2px solid #5577ff;">
                    <div style="font-size:13px;font-weight:bold;
                                letter-spacing:4px;color:#8da8ff;">
                      ⚔ DSABG ⚔
                    </div>
                    <h1 style="margin:18px 0 8px;font-size:27px;color:#ffffff;">
                      ENTER THE BATTLE
                    </h1>
                    <p style="margin:0;color:#b6c3df;font-size:14px;">
                      Verify your email to join the arena.
                    </p>
                  </div>

                  <div style="padding:30px 24px;text-align:center;">
                    <div style="font-size:12px;font-weight:bold;
                                letter-spacing:2px;color:#91a9ff;">
                      YOUR VERIFICATION CODE
                    </div>

                    <div style="margin:20px 0;padding:20px 10px;
                                background:#090f20;border:1px solid #3c5590;
                                border-radius:12px;">
                      <span style="font-size:36px;font-weight:bold;
                                   letter-spacing:10px;color:#ffffff;">
                        %s
                      </span>
                    </div>

                    <p style="font-size:14px;line-height:1.7;color:#c4cde0;">
                      Your code expires in
                      <strong style="color:#ffca70;">2 minutes</strong>.
                      Enter it on the DSABG signup page to continue.
                    </p>

                    <div style="margin-top:24px;padding:14px;
                                background:#201a15;border:1px solid #57432b;
                                border-radius:8px;text-align:left;">
                      <p style="margin:0;font-size:13px;line-height:1.6;
                                color:#f3d6a0;">
                        <strong>Security alert:</strong>
                        Never share this code with anyone.
                        If you didn't request it, simply ignore this email.
                      </p>
                    </div>
                  </div>

                  <div style="padding:20px;text-align:center;
                              border-top:1px solid #263451;">
                    <p style="margin:0;font-size:12px;color:#8592ad;">
                      BUILT FOR COMPETITORS. DESIGNED FOR BATTLES.
                    </p>
                    <p style="margin:8px 0 0;font-size:11px;color:#65718a;">
                      © DSABG — DSA Battle Grounds
                    </p>
                  </div>

                </div>
              </div>
            </body>
            </html>
            """.formatted(otp);
    }
}
