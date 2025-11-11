package com.takehome.gamengine.dto;

import java.util.UUID;
import lombok.Data;

@Data
public class GameMoveRequest {
    private UUID gameId;
    private Long playerId;
    private int row;
    private int col;
}