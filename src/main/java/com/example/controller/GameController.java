package com.example.controller;

import com.example.model.Game;
import com.example.model.Player;
import com.example.service.GameService;
import com.example.service.PlayerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/games")
public class GameController {
    
    private final GameService gameService;
    private final PlayerService playerService;
    
    @Autowired
    public GameController(GameService gameService, PlayerService playerService) {
        this.gameService = gameService;
        this.playerService = playerService;
    }
    
    // Create a new game
    @PostMapping
    public ResponseEntity<Game> createGame(@Valid @RequestBody CreateGameRequest request) {
        try {
            Game game = gameService.createGame(request.getName());
            return ResponseEntity.status(HttpStatus.CREATED).body(game);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Get game by ID
    @GetMapping("/{id}")
    public ResponseEntity<Game> getGame(@PathVariable String id) {
        return gameService.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    // Get all games
    @GetMapping
    public ResponseEntity<List<Game>> getAllGames(
            @RequestParam(required = false) String status) {
        List<Game> games;
        if (status != null) {
            try {
                Game.GameStatus gameStatus = Game.GameStatus.valueOf(status.toUpperCase());
                games = gameService.findByStatus(gameStatus);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().build();
            }
        } else {
            games = gameService.findAll();
        }
        return ResponseEntity.ok(games);
    }
    
    // Add player to game
    @PostMapping("/{id}/players")
    public ResponseEntity<Void> addPlayerToGame(
            @PathVariable String id,
            @Valid @RequestBody AddPlayerRequest request) {
        try {
            Player player = playerService.findById(request.getPlayerId())
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));
            
            boolean added = gameService.addPlayerToGame(id, player);
            return added ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Make a move
    @PostMapping("/{id}/moves")
    public ResponseEntity<Void> makeMove(
            @PathVariable String id,
            @Valid @RequestBody MakeMoveRequest request) {
        try {
            boolean success = gameService.makeMove(id, request.getPlayerId(), request.getPosition());
            return success ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Get game status
    @GetMapping("/{id}/status")
    public ResponseEntity<Map<String, String>> getGameStatus(@PathVariable String id) {
        try {
            Game.GameStatus status = gameService.getGameStatus(id);
            return ResponseEntity.ok(Map.of("status", status.name()));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Get game board
    @GetMapping("/{id}/board")
    public ResponseEntity<List<String>> getGameBoard(@PathVariable String id) {
        try {
            List<String> board = gameService.getGameBoard(id);
            return ResponseEntity.ok(board);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Get current player
    @GetMapping("/{id}/current-player")
    public ResponseEntity<Player> getCurrentPlayer(@PathVariable String id) {
        try {
            return gameService.getCurrentPlayer(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Get winner
    @GetMapping("/{id}/winner")
    public ResponseEntity<Player> getWinner(@PathVariable String id) {
        try {
            return gameService.getWinner(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Get game moves
    @GetMapping("/{id}/moves")
    public ResponseEntity<List<com.example.model.Move>> getGameMoves(@PathVariable String id) {
        try {
            List<com.example.model.Move> moves = gameService.getGameMoves(id);
            return ResponseEntity.ok(moves);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Delete game
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGame(@PathVariable String id) {
        boolean deleted = gameService.deleteGame(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
    
    // Get active games
    @GetMapping("/active")
    public ResponseEntity<List<Game>> getActiveGames() {
        List<Game> games = gameService.getActiveGames();
        return ResponseEntity.ok(games);
    }
    
    // Get waiting games
    @GetMapping("/waiting")
    public ResponseEntity<List<Game>> getWaitingGames() {
        List<Game> games = gameService.getWaitingGames();
        return ResponseEntity.ok(games);
    }
    
    // Get completed games
    @GetMapping("/completed")
    public ResponseEntity<List<Game>> getCompletedGames() {
        List<Game> games = gameService.getCompletedGames();
        return ResponseEntity.ok(games);
    }
    
    // Get games by player
    @GetMapping("/player/{playerId}")
    public ResponseEntity<List<Game>> getGamesByPlayer(@PathVariable String playerId) {
        try {
            List<Game> games = gameService.getGamesByPlayer(playerId);
            return ResponseEntity.ok(games);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Get total game count
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getGameCount() {
        long count = gameService.getTotalGameCount();
        return ResponseEntity.ok(Map.of("count", count));
    }
    
    // Request/Response DTOs
    public static class CreateGameRequest {
        private String name;
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
    }
    
    public static class AddPlayerRequest {
        private String playerId;
        
        public String getPlayerId() {
            return playerId;
        }
        
        public void setPlayerId(String playerId) {
            this.playerId = playerId;
        }
    }
    
    public static class MakeMoveRequest {
        private String playerId;
        private int position;
        
        public String getPlayerId() {
            return playerId;
        }
        
        public void setPlayerId(String playerId) {
            this.playerId = playerId;
        }
        
        public int getPosition() {
            return position;
        }
        
        public void setPosition(int position) {
            this.position = position;
        }
    }
}

// TODO: Implement basic leaderboard endpoint [ttt.feature.leaderboard.basic]