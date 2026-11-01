package com.dsabg.battleservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class DsabgBattleServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DsabgBattleServiceApplication.class, args);
    }

}
