package com.dsabg.dsabgauthservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.Map;

@RestController
@RequestMapping("/auth")
public class HealthController {
    @GetMapping("/health")
    public Map<String,String> health(){
       return Map.of("service","auth-service",
               "status","up"
               );
    }
}
