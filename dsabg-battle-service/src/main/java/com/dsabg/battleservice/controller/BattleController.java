package com.dsabg.battleservice.controller;

import com.dsabg.battleservice.dto.request.AcceptedSubmissionRequest;
import com.dsabg.battleservice.dto.request.CreateBattleRequest;
import com.dsabg.battleservice.dto.response.BattleProblemResponse;
import com.dsabg.battleservice.dto.response.BattleResponse;
import com.dsabg.battleservice.dto.response.BattleResultResponse;
import com.dsabg.battleservice.service.BattleService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/battle")
@RequiredArgsConstructor
public class BattleController {

    private final BattleService battleService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BattleResponse createBattle(
            @Valid @RequestBody CreateBattleRequest request) {

        return battleService.createBattle(request);
    }

    @GetMapping("/{battleId}/problems")
    public List<BattleProblemResponse> getBattleProblems(
            @PathVariable Long battleId) {

        return battleService.getBattleProblems(battleId);
    }

    @GetMapping("/{battleId}/results")
    public ResponseEntity<BattleResultResponse> getBattleResults(
            @PathVariable Long battleId
    ) {
        return ResponseEntity.ok(
                battleService.getBattleResults(battleId)
        );
    }

    @PostMapping("/internal/submission-accepted")
    public ResponseEntity<Void> markProblemSolved(
            @Valid @RequestBody AcceptedSubmissionRequest request
    ) {

        battleService.markProblemSolved(
                request.battleId(),
                request.problemId(),
                request.userId()
        );

        return ResponseEntity.ok().build();
    }
}