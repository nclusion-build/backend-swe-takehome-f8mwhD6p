package com.example.repository;

import com.example.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<Player, String> {
    
    // Find by email
    Optional<Player> findByEmail(String email);
    
    // Find by name (case-insensitive)
    List<Player> findByNameContainingIgnoreCase(String name);
    
    // Find players with games played above threshold
    @Query("SELECT p FROM Player p WHERE p.stats.gamesPlayed >= :minGames")
    List<Player> findPlayersWithMinGames(@Param("minGames") int minGames);
    
    // Find top players by win rate
    @Query("SELECT p FROM Player p WHERE p.stats.gamesPlayed > 0 ORDER BY p.stats.winRate DESC")
    List<Player> findTopPlayersByWinRate();
    
    // Find most active players
    @Query("SELECT p FROM Player p ORDER BY p.stats.gamesPlayed DESC")
    List<Player> findMostActivePlayers();
    
    // Find players by win rate range
    @Query("SELECT p FROM Player p WHERE p.stats.winRate BETWEEN :minRate AND :maxRate")
    List<Player> findPlayersByWinRateRange(@Param("minRate") double minRate, @Param("maxRate") double maxRate);
    
    // Count players by games played
    @Query("SELECT COUNT(p) FROM Player p WHERE p.stats.gamesPlayed >= :minGames")
    long countPlayersWithMinGames(@Param("minGames") int minGames);
    
    // Find players created in date range
    @Query("SELECT p FROM Player p WHERE p.createdAt BETWEEN :startDate AND :endDate")
    List<Player> findPlayersCreatedBetween(@Param("startDate") java.time.LocalDateTime startDate, 
                                         @Param("endDate") java.time.LocalDateTime endDate);
}

// TODO: Complete players routes (update, delete, search) [ttt.todo.routes.players.complete]