package com.dsabg.dsabgjudgeservice.service;

import com.dsabg.dsabgjudgeservice.dto.response.Judge0SubmissionResponse;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Service
public class Judge0Service {

    private final RestTemplate restTemplate = new RestTemplate();

    // Use wait=true for tests, but we MUST send base64 data to avoid hanging
    private final String JUDGE0_URL = "http://localhost:2358/submissions?wait=true";

    public Judge0SubmissionResponse execute(String sourceCode, int languageId, String stdin) {
        try {
            // 1. Transform raw multiline string into clean Base64 format
            String base64Code = Base64.getEncoder().encodeToString(sourceCode.getBytes());
            String base64Stdin = Base64.getEncoder().encodeToString(stdin.getBytes());

            // 2. Wrap properties matching Judge0 spec
            Map<String, Object> body = new HashMap<>();
            body.put("source_code", base64Code);
            body.put("language_id", languageId); // passed as 62 from controller
            body.put("stdin", base64Stdin);
            body.put("base64_encoded", true); // 🔥 This tells Judge0 to safely decode it inside the container

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            System.out.println("[Judge0Service] Invoking Judge0 API inside Ubuntu VM...");

            // 3. Post to Judge0 Sandbox
            ResponseEntity<Map> response = restTemplate.postForEntity(JUDGE0_URL, entity, Map.class);
            Map<String, Object> responseBody = response.getBody();

            System.out.println("[Judge0Service] Response received from Sandbox!");

            // 4. Transform response payload back to your custom response DTO
            return mapToCustomDto(responseBody);

        } catch (Exception e) {
            System.err.println("Execution exception: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Sandbox processing failure", e);
        }
    }

    private Judge0SubmissionResponse mapToCustomDto(Map<String, Object> responseBody) {
        Judge0SubmissionResponse responseDto = new Judge0SubmissionResponse();

        // Match status definitions from response map
        if (responseBody.get("status") != null) {
            Map<String, Object> statusMap = (Map<String, Object>) responseBody.get("status");
            System.out.println("Result Code Status: " + statusMap.get("description"));
            // responseDto.setStatus((String) statusMap.get("description"));
        }

        // Decode execution output cleanly if present
        if (responseBody.get("stdout") != null) {
            String decodedStdout = new String(Base64.getDecoder().decode(((String) responseBody.get("stdout")).trim()));
            System.out.println("Terminal Output: " + decodedStdout);
            // responseDto.setStdout(decodedStdout);
        }

        return responseDto;
    }
}
