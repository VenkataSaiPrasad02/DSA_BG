
package com.dsabg.battleservice.entity;

import com.dsabg.battleservice.enums.Team;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "battle_participants",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"battle_id", "user_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BattleParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "battle_id", nullable = false)
    private Battle battle;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Team team;

    @Column(nullable = false)
    private LocalDateTime joinedAt;
}
