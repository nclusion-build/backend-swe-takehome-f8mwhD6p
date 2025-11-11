package com.takehome.gamengine.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.takehome.gamengine.model.GameSession;
import lombok.Data;
import java.util.List;
import java.util.UUID;

// This DTO will be used for API responses, hiding complex DB objects.
@Data
public class GameDTO {
    private UUID id;
    private Long player1Id;
    private Long player2Id;
    private GameSession.GameStatus status;
    private Long winnerId;
    private List<Long> grid; // Send the grid as a proper JSON array
    private Long currentTurnPlayerId;
    private int moveCount;

    // This is a "mapper" to convert our DB model to our API response
    public GameDTO(GameSession game, ObjectMapper objectMapper) {
        this.id = game.getId();
        this.player1Id = game.getPlayer1() != null ? game.getPlayer1().getId() : null;
        this.player2Id = game.getPlayer2() != null ? game.getPlayer2().getId() : null;
        this.status = game.getStatus();
        this.winnerId = game.getWinner() != null ? game.getWinner().getId() : null;
        this.currentTurnPlayerId = game.getCurrentTurnPlayerId();
        this.moveCount = game.getMoveCount();
        
        // Deserialize the grid string into a List<Long>
        try {
            this.grid = objectMapper.readValue(game.getGrid(), new TypeReference<List<Long>>() {});
        } catch (JsonProcessingException e) {
            this.grid = List.of(); // Should not happen
        }
    }
}