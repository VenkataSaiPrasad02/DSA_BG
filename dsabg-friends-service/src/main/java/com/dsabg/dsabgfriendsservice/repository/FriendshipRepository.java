package com.dsabg.dsabgfriendsservice.repository;

import com.dsabg.dsabgfriendsservice.entity.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, Integer> {
    List<Friendship> findByPlayerOneIdOrPlayerTwoId(
            String playerOneId,
            String playerTwoId
    );

    boolean existsByPlayerOneIdAndPlayerTwoId(
            String playerOneId,
            String playerTwoId
    );

    Optional<Friendship> findByPlayerOneIdAndPlayerTwoId(
            String playerOneId,
            String playerTwoId
    );


}
