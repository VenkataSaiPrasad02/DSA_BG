
package com.dsabg.dsabgjudgeservice.controller;

import com.dsabg.dsabgjudgeservice.dto.response.Judge0SubmissionResponse;
import com.dsabg.dsabgjudgeservice.service.Judge0Service;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/judge/test")
@RequiredArgsConstructor
public class Judge0TestController {

    private final Judge0Service judge0Service;

    @GetMapping("/execute")
    public Judge0SubmissionResponse execute() {

        String sourceCode = """
                public class Main {
                    public static void main(String[] args) {
                        System.out.println("Hello Judge0");
                    }
                }
                """;

        return judge0Service.execute(
                sourceCode,
                62,
                ""
        );
    }
}
