package com.takehome.gamengine.repository;

import com.takehome.gamengine.model.GameSession;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;

@Repository
public interface GameSessionRepository extends JpaRepository<GameSession, UUID> {
    
    // This is the core of our concurrency strategy.
    // It locks the database row when we fetch it, so no other
    // transaction can modify it until this one is finished.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT g FROM GameSession g WHERE g.id = :id")
    Optional<GameSession> findByIdWithPessimisticLock(UUID id);
}