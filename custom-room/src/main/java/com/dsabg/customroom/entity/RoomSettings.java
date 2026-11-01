
package com.dsabg.customroom.entity;

import com.dsabg.customroom.entity.enums.Difficulty;
import com.dsabg.customroom.entity.enums.Topic;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "room_settings")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoomSettings {

    @Id
    @Column(name = "room_code")
    private Long roomCode;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "room_code")
    private Room room;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Difficulty difficulty = Difficulty.EASY;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "room_setting_topics",
            joinColumns = @JoinColumn(name = "room_code")
    )
    @Column(name = "topic", nullable = false, length = 40)
    @Enumerated(EnumType.STRING)
    private Set<Topic> topics = new HashSet<>(Set.of(Topic.ARRAYS));

    @Column(name = "number_of_problems", nullable = false)
    private Integer numberOfProblems = 1;

    public RoomSettings(Room room) {
        this.room = room;
        this.numberOfProblems = 1;
    }

    public void setTopics(Set<Topic> topics) {
        this.topics = topics == null
                ? new HashSet<>()
                : new HashSet<>(topics);
    }
}
