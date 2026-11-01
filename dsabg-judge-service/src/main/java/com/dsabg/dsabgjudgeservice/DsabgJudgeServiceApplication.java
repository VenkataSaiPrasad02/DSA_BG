package com.dsabg.dsabgjudgeservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class DsabgJudgeServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DsabgJudgeServiceApplication.class, args);
    }

}
