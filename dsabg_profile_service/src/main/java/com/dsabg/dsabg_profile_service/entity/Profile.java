
package com.dsabg.dsabg_profile_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@Table(
        name = "profiles",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_profile_user_id",
                        columnNames = "user_id"
                )
        }
)
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, length = 50)
    private String userId;

    @Column(name="display_name")
    private String displayName;

    @Column(length = 1000)
    private String avatarUrl;

    @Column(length = 500)
    private String bio;

    @Column(nullable = false)
    private int level = 1;

    @Column(nullable = false)
    private int battlesPlayed = 0;

    @Column(nullable = false)
    private int wins = 0;

    @Column(nullable = false)
    private int losses = 0;

    @Column(nullable = false)
    private int problemsSolved = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;


    public void updateLevel() {
        this.level = 1 + (this.wins / 10);
    }
}
