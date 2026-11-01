
package com.dsabg.battleservice.entity;

import com.dsabg.battleservice.enums.ProblemProgressStatus;
import com.dsabg.battleservice.enums.Team;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "battle_problem_progress",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"battle_problem_id", "team"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BattleProblemProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "battle_problem_id", nullable = false)
    private BattleProblem battleProblem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Team team;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProblemProgressStatus status;

    private String solvedBy;

    private LocalDateTime solvedAt;
}
