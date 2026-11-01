package com.dsabg.battleservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "dsabg-problem-service")
public interface ProblemServiceClient {

    @GetMapping("problem/internal/eligible")
    List<Long> getEligibleProblemIds(
            @RequestParam("categoryIds") List<Long> categoryIds,
            @RequestParam("difficulty") String difficulty
    );
}