package com.example.service;

import com.example.model.Player;
import com.example.model.PlayerStats;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class PlayerService {
    
    // In-memory storage for L2 (can be replaced with repository for L3)
    private final Map<String, Player> players = new ConcurrentHashMap<>();
    
    // Create a new player
    public Player createPlayer(String name, String email) {
        // Check if email already exists
        if (findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Player with this email already exists");
        }
        
        Player player = new Player(name, email);
        players.put(player.getId(), player);
        return player;
    }
    
    // Get player by ID
    public Optional<Player> findById(String id) {
        return Optional.ofNullable(players.get(id));
    }
    
    // Get player by email
    public Optional<Player> findByEmail(String email) {
        return players.values().stream()
            .filter(player -> player.getEmail().equals(email))
            .findFirst();
    }
    
    // Get all players
    public List<Player> findAll() {
        return new ArrayList<>(players.values());
    }
    
    // Update player
    public Player updatePlayer(String id, String name, String email) {
        Player player = findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Player not found"));
        
        // Check if email is being changed and if it conflicts with another player
        if (!player.getEmail().equals(email)) {
            findByEmail(email).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new IllegalArgumentException("Email already in use by another player");
                }
            });
        }
        
        player.setName(name);
        player.setEmail(email);
        return player;
    }
    
    // Delete player
    public boolean deletePlayer(String id) {
        return players.remove(id) != null;
    }
    
    // Search players by name (partial match)
    public List<Player> searchByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return findAll();
        }
        
        String searchTerm = name.toLowerCase().trim();
        return players.values().stream()
            .filter(player -> player.getName().toLowerCase().contains(searchTerm))
            .collect(Collectors.toList());
    }
    
    // Get player statistics
    public PlayerStats getPlayerStats(String id) {
        Player player = findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Player not found"));
        return player.getStats();
    }
    
    // Get leaderboard (top players by win rate)
    public List<Player> getLeaderboard(int limit) {
        return players.values().stream()
            .filter(player -> player.getStats().getGamesPlayed() > 0)
            .sorted((p1, p2) -> Double.compare(p2.getStats().getWinRate(), p1.getStats().getWinRate()))
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    // Get players with most games played
    public List<Player> getMostActivePlayers(int limit) {
        return players.values().stream()
            .sorted((p1, p2) -> Integer.compare(p2.getStats().getGamesPlayed(), p1.getStats().getGamesPlayed()))
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    // Get players with highest efficiency
    public List<Player> getMostEfficientPlayers(int limit) {
        return players.values().stream()
            .filter(player -> player.getStats().getGamesWon() > 0)
            .sorted((p1, p2) -> Double.compare(p2.getStats().getEfficiency(), p1.getStats().getEfficiency()))
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    // Update player stats after game completion
    public void updatePlayerStats(String playerId, boolean won, boolean drawn, int movesMade) {
        Player player = findById(playerId)
            .orElseThrow(() -> new IllegalArgumentException("Player not found"));
        
        PlayerStats stats = player.getStats();
        stats.incrementGamesPlayed();
        
        if (won) {
            stats.incrementGamesWon();
        } else if (drawn) {
            stats.incrementGamesDrawn();
        } else {
            stats.incrementGamesLost();
        }
        
        stats.addMoves(movesMade);
    }
    
    // Get total player count
    public long getTotalPlayerCount() {
        return players.size();
    }
    
    // Get players created in date range
    public List<Player> getPlayersCreatedBetween(Date startDate, Date endDate) {
        return players.values().stream()
            .filter(player -> {
                Date createdAt = java.sql.Timestamp.valueOf(player.getCreatedAt());
                return createdAt.after(startDate) && createdAt.before(endDate);
            })
            .collect(Collectors.toList());
    }
}

// TODO: Add player email validation [ttt.todo.validation.player-email]
// TODO: Complete players routes (update, delete, search) [ttt.todo.routes.players.complete]
// TODO: Implement PlayerService (create/get/update/delete/search/stats) [ttt.todo.service.player.complete]