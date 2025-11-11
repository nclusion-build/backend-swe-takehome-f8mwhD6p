package com.takehome.gamengine.dto;

import java.util.UUID;
import lombok.Data;

@Data
public class JoinGameRequest {
    private UUID gameId;
    private Long player2Id;
}