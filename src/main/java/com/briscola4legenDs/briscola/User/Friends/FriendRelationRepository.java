package com.briscola4legenDs.briscola.User.Friends;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendRelationRepository extends JpaRepository<FriendRelation, Integer> {
    @Query("SELECT f FROM FriendRelation f WHERE f.userId = ?1")
    List<FriendRelation> findByUserId(long userId);

    @Query("SELECT f.id FROM FriendRelation f WHERE f.userId = ?1 AND f.friendId =?2")
    long findIDByRelation(long userId, long friendId);
}
