package com.takehome.gamengine.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.takehome.gamengine.dto.GameDTO;
import com.takehome.gamengine.dto.GameMoveRequest;
import com.takehome.gamengine.dto.JoinGameRequest;
import com.takehome.gamengine.model.GameSession;
import com.takehome.gamengine.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/game")
public class GameController {

    @Autowired
    private GameService gameService;
    
    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping("/create")
    public ResponseEntity<GameDTO> createGame(@RequestBody Map<String, Long> payload) {
        Long player1Id = payload.get("player1_id");
        GameSession game = gameService.createGame(player1Id);
        return new ResponseEntity<>(new GameDTO(game, objectMapper), HttpStatus.CREATED);
    }

    @PostMapping("/join")
    public ResponseEntity<GameDTO> joinGame(@RequestBody JoinGameRequest request) {
        GameSession game = gameService.joinGame(request.getGameId(), request.getPlayer2Id());
        return ResponseEntity.ok(new GameDTO(game, objectMapper));
    }

    @PostMapping("/move")
    public ResponseEntity<GameDTO> makeMove(@RequestBody GameMoveRequest request) {
        GameSession game = gameService.makeMove(
            request.getGameId(), 
            request.getPlayerId(), 
            request.getRow(), 
            request.getCol()
        );
        return ResponseEntity.ok(new GameDTO(game, objectMapper));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GameDTO> getGame(@PathVariable UUID id) {
        GameSession game = gameService.getGame(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));
        return ResponseEntity.ok(new GameDTO(game, objectMapper));
    }
}