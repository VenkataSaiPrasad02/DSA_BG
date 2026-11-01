package com.dsabg.problem.controller;

import com.dsabg.problem.dto.request.PatternRequest;
import com.dsabg.problem.entity.Pattern;
import com.dsabg.problem.service.PatternService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/patterns")
@RequiredArgsConstructor
public class PatternController {

    private final PatternService patternService;

    @PostMapping
    public ResponseEntity<Pattern> createPattern(
            @RequestBody PatternRequest request) {

        Pattern pattern = patternService.createPattern(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(pattern);
    }
}