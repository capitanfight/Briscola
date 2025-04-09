package com.briscola4legenDs.briscola.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query("SELECT s FROM User s WHERE s.username = ?1")
    Optional<User> findByUsername(String username);

    @Query("SELECT s FROM User s WHERE s.email = ?1")
    Optional<User> findByEmail(String email);
}
