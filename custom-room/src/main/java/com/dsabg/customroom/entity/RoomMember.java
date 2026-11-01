package com.dsabg.customroom.entity;

import com.dsabg.customroom.entity.enums.Team;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;


@Entity
@Data
@Table(
        name = "room_members",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_room_team_slot",
                        columnNames = {"room_code", "team", "slot_number"}
                )
        }
)
public class RoomMember {

    @Id
    @Column(name = "user_id", nullable = false ,updatable = false)
    private String userId;

    @Column(nullable = false)
    private String displayName;

    @ManyToOne(fetch=FetchType.LAZY,optional = false)
    @JoinColumn(name = "room_code",nullable = false)
    private Room room;

    @Column(name = "slot_number", nullable = false)
    private Integer slotNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 1)
    private Team team;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt;
}
