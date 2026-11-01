package com.dsabg.dsabgfriendsservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@Table(
        name = "friendships",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_friendship_players",
                        columnNames = {"player_one_id", "player_two_id"}
                )
        }
)
public class Friendship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "player_one_id", nullable = false, length = 50)
    private String playerOneId;

    @Column(name = "player_two_id", nullable = false, length = 50)
    private String playerTwoId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}