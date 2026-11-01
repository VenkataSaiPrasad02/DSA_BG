package com.dsabg.dsabgauthservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.ApplicationRunner;

import org.springframework.amqp.support.converter.MessageConverter;

@Configuration
@EnableRabbit
public class RabbitMQConfig {
    private static final Logger log =
            LoggerFactory.getLogger(RabbitMQConfig.class);
    public static final  String EMAIL_EXCHANGE_NAME = "dsabg.email.exchange";
    public static final String EMAIL_QUEUE_NAME = "dsabg.email.queue";
    public static final String EMAIL_ROUTING_KEY = "dsabg.email.send";



    public static final String USER_REGISTERED_EXCHANGE = "dsabg.user.exchange";
    public static final String USER_REGISTERED_QUEUE = "dsabg.user.registered.queue";
    public static final String USER_REGISTERED_ROUTING_KEY = "dsabg.user.registered";


    @Bean
    public DirectExchange emailExchange() {
        return new DirectExchange(EMAIL_EXCHANGE_NAME,true,false);
    }
    @Bean
    public DirectExchange userExchange() {
        return new DirectExchange(USER_REGISTERED_EXCHANGE,true,false);
    }
    @Bean
    public Queue emailQueue() {
        log.info("RabbitMQConfig loaded: declaring queue {}",
                EMAIL_QUEUE_NAME);
        return new Queue(EMAIL_QUEUE_NAME, true);
    }
    @Bean
    public Queue userRegisteredQueue() {
        return  new Queue(USER_REGISTERED_QUEUE, true);
    }
    @Bean
    public Binding emailBinding() {
        return  BindingBuilder
                .bind(emailQueue())
                .to(emailExchange())
                .with(EMAIL_ROUTING_KEY);
    }
    @Bean
    public Binding userRegisteredBinding() {
        return BindingBuilder
                .bind(userRegisteredQueue())
                .to(userExchange())
                .with(USER_REGISTERED_ROUTING_KEY);
    }
    @Bean
    public ApplicationRunner rabbitMQConnectionTest(
            ConnectionFactory connectionFactory) {

        return args -> {
            try (var connection = connectionFactory.createConnection()) {
                log.info("RabbitMQ connection successful: {}",
                        connection.isOpen());
            }
        };
    }
    @Bean
    public MessageConverter rabbitMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

}
