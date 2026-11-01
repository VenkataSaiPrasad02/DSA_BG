
package com.dsabg.battleservice.service.impl;

import com.dsabg.battleservice.client.ProblemServiceClient;
import com.dsabg.battleservice.dto.request.CreateBattleRequest;
import com.dsabg.battleservice.dto.request.ParticipantRequest;
import com.dsabg.battleservice.dto.response.*;
import com.dsabg.battleservice.entity.*;
import com.dsabg.battleservice.enums.*;
import com.dsabg.battleservice.repository.*;
import com.dsabg.battleservice.service.BattleService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BattleServiceImpl implements BattleService {

    private final BattleRepository battleRepository;
    private final BattleProblemRepository battleProblemRepository;
    private final BattleProblemProgressRepository progressRepository;
    private final BattleParticipantRepository participantRepository;
    private final ProblemServiceClient problemServiceClient;

    @Override
    @Transactional
    public BattleResponse createBattle(CreateBattleRequest request) {

        // 1. Fetch eligible problem IDs
        List<Long> eligibleProblemIds =
                problemServiceClient.getEligibleProblemIds(
                        request.categoryIds(),
                        request.difficulty().name()
                );

        // 2. Validate availability
        if (eligibleProblemIds.size() < request.numberOfProblems()) {
            throw new IllegalArgumentException(
                    "Not enough eligible problems available"
            );
        }

        // 3. Randomly select problems
        List<Long> selectedProblemIds =
                new ArrayList<>(eligibleProblemIds);

        Collections.shuffle(selectedProblemIds);

        List<Long> allocatedProblemIds = new ArrayList<>(
                selectedProblemIds.subList(0, request.numberOfProblems())
        );

        // 4. Validate duplicate participants
        long uniqueUsers = request.participants()
                .stream()
                .map(ParticipantRequest::userId)
                .distinct()
                .count();

        if (uniqueUsers != request.participants().size()) {
            throw new IllegalArgumentException(
                    "Duplicate participants found"
            );
        }

        // 5. Generate battle number
        Integer battleNumber = battleRepository
                .findFirstByRoomIdOrderByBattleNumberDesc(request.roomId())
                .map(battle -> battle.getBattleNumber() + 1)
                .orElse(1);

        // 6. Create battle
        LocalDateTime now = LocalDateTime.now();

        Battle battle = Battle.builder()
                .roomId(request.roomId())
                .battleNumber(battleNumber)
                .status(BattleStatus.IN_BATTLE)
                .totalProblems(request.numberOfProblems())
                .createdAt(now)
                .startedAt(now)
                .build();

        battleRepository.save(battle);

        // 7. Save participants
        List<BattleParticipant> participants =
                request.participants().stream()
                        .map(participant -> BattleParticipant.builder()
                                .battle(battle)
                                .userId(participant.userId())
                                .displayName(participant.displayName())
                                .team(participant.team())
                                .joinedAt(now)
                                .build())
                        .toList();

        participantRepository.saveAll(participants);

        // 8. Save allocated problems
        List<BattleProblem> battleProblems = new ArrayList<>();

        for (int i = 0; i < allocatedProblemIds.size(); i++) {

            BattleProblem battleProblem = BattleProblem.builder()
                    .battle(battle)
                    .problemId(allocatedProblemIds.get(i))
                    .sequenceNumber(i + 1)
                    .build();

            battleProblems.add(battleProblem);
        }

        battleProblemRepository.saveAll(battleProblems);

        // 9. Create initial progress for both teams
        List<BattleProblemProgress> progressRecords = new ArrayList<>();

        for (BattleProblem battleProblem : battleProblems) {

            for (Team team : Team.values()) {

                progressRecords.add(
                        BattleProblemProgress.builder()
                                .battleProblem(battleProblem)
                                .team(team)
                                .status(ProblemProgressStatus.PENDING)
                                .build()
                );
            }
        }

        progressRepository.saveAll(progressRecords);

        // 10. Return response
        return new BattleResponse(
                battle.getBattleId(),
                battle.getRoomId(),
                battle.getBattleNumber(),
                battle.getTotalProblems(),
                battle.getStatus().name(),
                battle.getStartedAt()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<BattleProblemResponse> getBattleProblems(Long battleId) {

        if (!battleRepository.existsById(battleId)) {
            throw new RuntimeException("Battle not found");
        }

        return battleProblemRepository
                .findByBattleBattleIdOrderBySequenceNumberAsc(battleId)
                .stream()
                .map(problem -> new BattleProblemResponse(
                        problem.getProblemId(),
                        problem.getSequenceNumber()
                ))
                .toList();
    }

    @Override
    @Transactional
    public void markProblemSolved(
            Long battleId,
            Long problemId,
            String userId
    ) {

        // 1. Lock battle row FIRST
        Battle battle = battleRepository.findByIdForUpdate(battleId)
                .orElseThrow(() -> new RuntimeException("Battle not found"));

        // 2. Check battle status
        if (battle.getStatus() != BattleStatus.IN_BATTLE) {
            return;
        }

        // 3. Verify participant
        BattleParticipant participant = participantRepository
                .findByBattleBattleIdAndUserId(battleId, userId)
                .orElseThrow(() -> new RuntimeException(
                        "Participant not found in this battle"
                ));

        // 4. Verify problem allocation
        BattleProblem battleProblem = battleProblemRepository
                .findByBattleBattleIdAndProblemId(battleId, problemId)
                .orElseThrow(() -> new RuntimeException(
                        "Problem not allocated to this battle"
                ));

        // 5. Get progress for participant's team
        BattleProblemProgress progress = progressRepository
                .findByBattleProblemIdAndTeam(
                        battleProblem.getId(),
                        participant.getTeam()
                )
                .orElseThrow(() -> new RuntimeException(
                        "Team progress not found"
                ));

        // 6. Prevent duplicate updates
        if (progress.getStatus() == ProblemProgressStatus.SOLVED) {
            return;
        }

        // 7. Mark problem as solved
        progress.setStatus(ProblemProgressStatus.SOLVED);
        progress.setSolvedBy(userId);
        progress.setSolvedAt(LocalDateTime.now());

        progressRepository.save(progress);

        // 8. Check whether this team completed all problems
        if (hasCompletedAllProblems(battleId, participant.getTeam())) {

            battle.setStatus(BattleStatus.COMPLETED);
            battle.setWinningTeam(participant.getTeam());
            battle.setEndedAt(LocalDateTime.now());

            battleRepository.save(battle);
        }
    }

    // Check whether all problems are solved by a team
    private boolean hasCompletedAllProblems(Long battleId, Team team) {

        List<BattleProblemProgress> progressRecords =
                progressRepository.findByBattleProblemBattleBattleIdAndTeam(
                        battleId, team
                );

        return !progressRecords.isEmpty()
                && progressRecords.stream()
                .allMatch(progress ->
                        progress.getStatus() == ProblemProgressStatus.SOLVED
                );
    }

    @Override
    @Transactional(readOnly = true)
    public BattleResultResponse getBattleResults(Long battleId) {

        Battle battle = battleRepository.findById(battleId)
                .orElseThrow(() -> new RuntimeException("Battle not found"));

        List<TeamResultResponse> teamResults = new ArrayList<>();

        for (Team team : Team.values()) {

            // Get team progress
            List<BattleProblemProgress> progressRecords =
                    progressRepository
                            .findByBattleProblemBattleBattleIdAndTeam(
                                    battleId, team
                            );

            long solvedCount = progressRecords.stream()
                    .filter(p -> p.getStatus() == ProblemProgressStatus.SOLVED)
                    .count();

            // Get team participants
            List<BattleParticipant> participants =
                    participantRepository.findByBattleBattleIdAndTeam(
                            battleId, team
                    );

            // Calculate MVP
            PlayerMvpResponse mvp = calculateMvp(participants, progressRecords);

            // Calculate team completion time
            Long completionTimeMillis = null;

            if (solvedCount == battle.getTotalProblems()) {

                LocalDateTime lastSolvedAt = progressRecords.stream()
                        .map(BattleProblemProgress::getSolvedAt)
                        .max(LocalDateTime::compareTo)
                        .orElse(null);

                if (lastSolvedAt != null) {
                    completionTimeMillis = Duration.between(
                            battle.getStartedAt(),
                            lastSolvedAt
                    ).toMillis();
                }
            }

            teamResults.add(new TeamResultResponse(
                    team,
                    battle.getTotalProblems(),
                    (int) solvedCount,
                    completionTimeMillis,
                    mvp
            ));
        }

        return new BattleResultResponse(
                battle.getBattleId(),
                battle.getRoomId(),
                battle.getStatus(),
                battle.getWinningTeam(),
                battle.getStartedAt(),
                battle.getEndedAt(),
                teamResults
        );
    }

    private PlayerMvpResponse calculateMvp(
            List<BattleParticipant> participants,
            List<BattleProblemProgress> progressList
    ) {

        Map<String, Long> solvedCount = progressList.stream()
                .filter(progress -> progress.getSolvedBy() != null)
                .collect(Collectors.groupingBy(
                        BattleProblemProgress::getSolvedBy,
                        Collectors.counting()
                ));

        return participants.stream()
                .map(participant -> new PlayerMvpResponse(
                        participant.getUserId(),
                        participant.getDisplayName(),
                        solvedCount.getOrDefault(participant.getUserId(), 0L).intValue()
                ))
                .filter(player -> player.problemsSolved() > 0)
                .max(Comparator.comparingInt(PlayerMvpResponse::problemsSolved))
                .orElse(null);
    }
}
