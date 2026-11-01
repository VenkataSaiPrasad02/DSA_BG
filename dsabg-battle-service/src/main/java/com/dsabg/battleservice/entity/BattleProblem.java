
package com.dsabg.battleservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "battle_problems",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"battle_id", "problem_id"}
                ),
                @UniqueConstraint(
                        columnNames = {"battle_id", "sequence_number"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BattleProblem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "battle_id", nullable = false)
    private Battle battle;

    @Column(nullable = false)
    private Long problemId;

    @Column(nullable = false)
    private Integer sequenceNumber;
}
