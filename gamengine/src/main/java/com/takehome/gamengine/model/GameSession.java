package com.takehome.gamengine.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class GameSession {

    public enum GameStatus {
        WAITING_FOR_PLAYER,
        IN_PROGRESS,
        FINISHED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User player1;

    @ManyToOne(fetch = FetchType.LAZY)
    private User player2;

    @Enumerated(EnumType.STRING)
    private GameStatus status = GameStatus.WAITING_FOR_PLAYER;

    @ManyToOne(fetch = FetchType.LAZY)
    private User winner;

    // Grid will be stored as a 9-char string. "1N2N1NN2N"
    // N = null, 1 = player1.id, 2 = player2.id (using IDs)
    // We will store player *IDs* as per the prompt.
    @Column(length = 1024) // Store grid as JSON string: "[null,1,2,null,null,1,null,2,null]"
    private String grid = "[null,null,null,null,null,null,null,null,null]";
    
    private Long currentTurnPlayerId;
    
    private int moveCount = 0;

    public GameSession(User player1) {
        this.player1 = player1;
    }
}