
package com.dsabg.customroom.entity;

import com.dsabg.customroom.entity.enums.RoomStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "rooms")
@Getter
@Setter
@NoArgsConstructor
public class Room {

    @Id
    @Column(name = "room_code", nullable = false, updatable = false, length = 10)
    private Long roomCode;

    @Column(nullable = false, length = 60)
    private String roomName;

    @Column(name = "host_user_id", nullable = false, length = 100)
    private String hostUserId;

    @Column(nullable = false)
    private String hostDisplayName;

    @Column(name = "password_hash")
    @JsonIgnore
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoomStatus status;

    @Column(name = "version")
    @Version
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;


}
