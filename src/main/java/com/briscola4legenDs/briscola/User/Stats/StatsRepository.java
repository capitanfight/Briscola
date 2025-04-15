package com.briscola4legenDs.briscola.User.Stats;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StatsRepository extends JpaRepository<Stats, Integer> {
    @Query("SELECT s FROM Stats s WHERE s.id = ?1")
    Optional<Stats> findById(long id);

    @Query("SELECT s FROM Stats s ORDER BY s.win DESC")
    List<Stats> findAllByOrderByWinDesc();
}