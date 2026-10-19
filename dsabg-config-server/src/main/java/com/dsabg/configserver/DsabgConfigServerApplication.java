package com.dsabg.configserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication
@EnableConfigServer
public class DsabgConfigServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DsabgConfigServerApplication.class, args);
    }

}
