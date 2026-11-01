package com.dsabg.dsabgeurekaserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class DsabgEurekaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DsabgEurekaServerApplication.class, args);
    }

}
