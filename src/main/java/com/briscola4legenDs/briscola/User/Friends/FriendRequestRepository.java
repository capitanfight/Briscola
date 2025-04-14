package com.briscola4legenDs.briscola.User.Friends;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequest, Integer> {
    @Query("SELECT f FROM FriendRequest f WHERE f.friendId = ?1")
    List<FriendRequest> findByFriendId(long friendId);
}
