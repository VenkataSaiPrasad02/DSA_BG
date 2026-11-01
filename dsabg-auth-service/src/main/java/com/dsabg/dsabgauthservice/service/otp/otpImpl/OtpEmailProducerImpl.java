package com.dsabg.dsabgauthservice.service.otp.otpImpl;

import com.dsabg.dsabgauthservice.config.RabbitMQConfig;
import com.dsabg.dsabgauthservice.dto.request.OtpEmailMessage;
import com.dsabg.dsabgauthservice.service.otp.OtpEmailProducer;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class OtpEmailProducerImpl implements OtpEmailProducer {
    private final RabbitTemplate rabbitTemplate;

    public OtpEmailProducerImpl(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publishOtpEmail(String email, String otp) {
        OtpEmailMessage message = new OtpEmailMessage(email, otp);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EMAIL_EXCHANGE_NAME,
                RabbitMQConfig.EMAIL_ROUTING_KEY,
                message);


    }
}
