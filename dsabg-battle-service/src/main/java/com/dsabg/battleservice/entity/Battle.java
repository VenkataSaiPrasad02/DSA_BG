
package com.dsabg.battleservice.entity;

import com.dsabg.battleservice.enums.BattleStatus;
import com.dsabg.battleservice.enums.Team;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "battles",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"room_id", "battle_number"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Battle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long battleId;

    @Column(nullable = false)
    private Long roomId;

    @Column(nullable = false)
    private Integer battleNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BattleStatus status;

    @Column(nullable = false)
    private Integer totalProblems;

    @Enumerated(EnumType.STRING)
    private Team winningTeam;

    private LocalDateTime startedAt;

    private LocalDateTime endedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
