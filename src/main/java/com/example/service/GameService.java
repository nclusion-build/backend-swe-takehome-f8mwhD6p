package com.example.service;

import com.example.model.Game;
import com.example.model.Player;
import com.example.model.Move;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class GameService {
    
    // In-memory storage for L2 (can be replaced with repository for L3)
    private final Map<String, Game> games = new ConcurrentHashMap<>();
    
    // Create a new game
    public Game createGame(String name) {
        Game game = new Game(name);
        games.put(game.getId(), game);
        return game;
    }
    
    // Get game by ID
    public Optional<Game> findById(String id) {
        return Optional.ofNullable(games.get(id));
    }
    
    // Get all games
    public List<Game> findAll() {
        return new ArrayList<>(games.values());
    }
    
    // Get games by status
    public List<Game> findByStatus(Game.GameStatus status) {
        return games.values().stream()
            .filter(game -> game.getStatus() == status)
            .collect(Collectors.toList());
    }
    
    // Get active games
    public List<Game> getActiveGames() {
        return findByStatus(Game.GameStatus.ACTIVE);
    }
    
    // Get waiting games
    public List<Game> getWaitingGames() {
        return findByStatus(Game.GameStatus.WAITING);
    }
    
    // Get completed games
    public List<Game> getCompletedGames() {
        return games.values().stream()
            .filter(game -> game.getStatus() == Game.GameStatus.COMPLETED || 
                          game.getStatus() == Game.GameStatus.DRAW)
            .collect(Collectors.toList());
    }
    
    // Add player to game
    public boolean addPlayerToGame(String gameId, Player player) {
        Game game = findById(gameId)
            .orElseThrow(() -> new IllegalArgumentException("Game not found"));
        
        if (game.getStatus() != Game.GameStatus.WAITING) {
            throw new IllegalStateException("Game is not accepting new players");
        }
        
        if (game.getPlayers().size() >= 2) {
            throw new IllegalStateException("Game is full");
        }
        
        return game.addPlayer(player);
    }
    
    // Make a move in a game
    public boolean makeMove(String gameId, String playerId, int position) {
        Game game = findById(gameId)
            .orElseThrow(() -> new IllegalArgumentException("Game not found"));
        
        Player player = game.getPlayers().stream()
            .filter(p -> p.getId().equals(playerId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Player not in this game"));
        
        if (game.getStatus() != Game.GameStatus.ACTIVE) {
            throw new IllegalStateException("Game is not active");
        }
        
        if (!game.getCurrentPlayer().getId().equals(playerId)) {
            throw new IllegalStateException("Not player's turn");
        }
        
        return game.makeMove(player, position);
    }
    
    // Get game status
    public Game.GameStatus getGameStatus(String gameId) {
        Game game = findById(gameId)
            .orElseThrow(() -> new IllegalArgumentException("Game not found"));
        return game.getStatus();
    }
    
    // Get game board
    public List<String> getGameBoard(String gameId) {
        Game game = findById(gameId)
            .orElseThrow(() -> new IllegalArgumentException("Game not found"));
        return new ArrayList<>(game.getBoard());
    }
    
    // Get current player
    public Optional<Player> getCurrentPlayer(String gameId) {
        Game game = findById(gameId)
            .orElseThrow(() -> new IllegalArgumentException("Game not found"));
        return Optional.ofNullable(game.getCurrentPlayer());
    }
    
    // Get winner
    public Optional<Player> getWinner(String gameId) {
        Game game = findById(gameId)
            .orElseThrow(() -> new IllegalArgumentException("Game not found"));
        return Optional.ofNullable(game.getWinner());
    }
    
    // Get game moves
    public List<Move> getGameMoves(String gameId) {
        Game game = findById(gameId)
            .orElseThrow(() -> new IllegalArgumentException("Game not found"));
        return new ArrayList<>(game.getMoves());
    }
    
    // Delete game
    public boolean deleteGame(String id) {
        return games.remove(id) != null;
    }
    
    // Get games by player
    public List<Game> getGamesByPlayer(String playerId) {
        return games.values().stream()
            .filter(game -> game.getPlayers().stream()
                .anyMatch(player -> player.getId().equals(playerId)))
            .collect(Collectors.toList());
    }
    
    // Get player's active games
    public List<Game> getPlayerActiveGames(String playerId) {
        return getGamesByPlayer(playerId).stream()
            .filter(game -> game.getStatus() == Game.GameStatus.ACTIVE)
            .collect(Collectors.toList());
    }
    
    // Get total game count
    public long getTotalGameCount() {
        return games.size();
    }
    
    // Get games created in date range
    public List<Game> getGamesCreatedBetween(Date startDate, Date endDate) {
        return games.values().stream()
            .filter(game -> {
                Date createdAt = java.sql.Timestamp.valueOf(game.getCreatedAt());
                return createdAt.after(startDate) && createdAt.before(endDate);
            })
            .collect(Collectors.toList());
    }
    
    // Get games with most moves
    public List<Game> getGamesWithMostMoves(int limit) {
        return games.values().stream()
            .sorted((g1, g2) -> Integer.compare(g2.getMoves().size(), g1.getMoves().size()))
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    // Get recent games
    public List<Game> getRecentGames(int limit) {
        return games.values().stream()
            .sorted((g1, g2) -> g2.getCreatedAt().compareTo(g1.getCreatedAt()))
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    // Check if game is full
    public boolean isGameFull(String gameId) {
        Game game = findById(gameId)
            .orElseThrow(() -> new IllegalArgumentException("Game not found"));
        return game.getPlayers().size() >= 2;
    }
    
    // Check if player is in game
    public boolean isPlayerInGame(String gameId, String playerId) {
        Game game = findById(gameId)
            .orElseThrow(() -> new IllegalArgumentException("Game not found"));
        return game.getPlayers().stream()
            .anyMatch(player -> player.getId().equals(playerId));
    }
}

// TODO: Implement basic leaderboard endpoint [ttt.feature.leaderboard.basic]