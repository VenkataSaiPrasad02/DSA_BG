package com.dsabg.dsabgfriendsservice.controller;

import com.dsabg.dsabgfriendsservice.dto.response.PlayerSearchResponse;
import com.dsabg.dsabgfriendsservice.service.PlayerSearchService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/player")
@RequiredArgsConstructor
public class PlayerSearchController {

    private final PlayerSearchService playerSearchService;

    @GetMapping("/search/{userId}")
    public ResponseEntity<PlayerSearchResponse> searchPlayerById(
            @PathVariable String userId,
            @RequestHeader("X-User-Id") String currentUserId) {

        return ResponseEntity.ok(
                playerSearchService.searchPlayerById(
                        currentUserId, userId));
    }
}