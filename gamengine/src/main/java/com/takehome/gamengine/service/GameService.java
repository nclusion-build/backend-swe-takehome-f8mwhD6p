package com.takehome.gamengine.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.takehome.gamengine.model.GameSession;
import com.takehome.gamengine.model.User;
import com.takehome.gamengine.repository.GameSessionRepository;
import com.takehome.gamengine.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

@Service
public class GameService {

    @Autowired
    private GameSessionRepository gameSessionRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ObjectMapper objectMapper; // Spring Boot configures this for us

    private static final int[][] WIN_CONDITIONS = {
        {0, 1, 2}, {3, 4, 5}, {6, 7, 8}, // Rows
        {0, 3, 6}, {1, 4, 7}, {2, 5, 8}, // Columns
        {0, 4, 8}, {2, 4, 6}  // Diagonals
    };

    @Transactional
    public GameSession createGame(Long player1Id) {
        User player1 = userRepository.findById(player1Id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Player 1 not found"));
        
        GameSession game = new GameSession(player1);
        return gameSessionRepository.save(game);
    }

    @Transactional
    public GameSession joinGame(UUID gameId, Long player2Id) {
        // Find the game and lock it for this transaction
        GameSession game = gameSessionRepository.findByIdWithPessimisticLock(gameId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));

        User player2 = userRepository.findById(player2Id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Player 2 not found"));

        if (game.getStatus() != GameSession.GameStatus.WAITING_FOR_PLAYER) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Game is not waiting for a player");
        }
        if (game.getPlayer1().getId().equals(player2Id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot play against yourself");
        }

        game.setPlayer2(player2);
        game.setStatus(GameSession.GameStatus.IN_PROGRESS);
        game.setCurrentTurnPlayerId(game.getPlayer1().getId()); // Player 1 always starts
        
        return gameSessionRepository.save(game);
    }

    @Transactional
    public GameSession makeMove(UUID gameId, Long playerId, int row, int col) {
        // Find and lock the game row for concurrency
        GameSession game = gameSessionRepository.findByIdWithPessimisticLock(gameId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));

        // --- 1. Validation ---
        if (game.getStatus() != GameSession.GameStatus.IN_PROGRESS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Game is not in progress");
        }
        if (!game.getCurrentTurnPlayerId().equals(playerId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not your turn");
        }
        if (!game.getPlayer1().getId().equals(playerId) && (game.getPlayer2() == null || !game.getPlayer2().getId().equals(playerId))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Player is not part of this game");
        }
        if (row < 0 || row > 2 || col < 0 || col > 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid cell coordinates");
        }
        
        List<Long> grid = parseGrid(game.getGrid());
        int gridIndex = row * 3 + col;

        if (grid.get(gridIndex) != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cell is already occupied");
        }

        // --- 2. Apply Move ---
        grid.set(gridIndex, playerId);
        game.setGrid(serializeGrid(grid));
        game.setMoveCount(game.getMoveCount() + 1);

        // --- 3. Check Game Over ---
        String result = checkWinner(grid, playerId, game.getMoveCount());
        
        if ("win".equals(result)) {
            game.setStatus(GameSession.GameStatus.FINISHED);
            game.setWinner(userRepository.findById(playerId).get()); // We know this user exists
            game.setCurrentTurnPlayerId(null);
            updatePlayerStats(game, playerId, false);

        } else if ("draw".equals(result)) {
            game.setStatus(GameSession.GameStatus.FINISHED);
            game.setCurrentTurnPlayerId(null);
            updatePlayerStats(game, null, true); // null winner, is a draw

        } else { // Game continues
            // Swap turns
            game.setCurrentTurnPlayerId(
                playerId.equals(game.getPlayer1().getId()) ? game.getPlayer2().getId() : game.getPlayer1().getId()
            );
        }

        return gameSessionRepository.save(game);
    }
    
    public Optional<GameSession> getGame(UUID gameId) {
        return gameSessionRepository.findById(gameId);
    }

    // --- Private Helper Methods ---

    private List<Long> parseGrid(String gridJson) {
        try {
            return objectMapper.readValue(gridJson, new TypeReference<List<Long>>() {});
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse game grid", e);
        }
    }

    private String serializeGrid(List<Long> gridList) {
        try {
            return objectMapper.writeValueAsString(gridList);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize game grid", e);
        }
    }

    private String checkWinner(List<Long> grid, Long playerId, int moveCount) {
        for (int[] condition : WIN_CONDITIONS) {
            if (grid.get(condition[0]) != null &&
                grid.get(condition[0]) == playerId &&
                grid.get(condition[1]) == playerId &&
                grid.get(condition[2]) == playerId) {
                return "win";
            }
        }
        if (moveCount == 9) {
            return "draw";
        }
        return "continue";
    }

    private void updatePlayerStats(GameSession game, Long winnerId, boolean isDraw) {
        User player1 = game.getPlayer1();
        User player2 = game.getPlayer2();
        
        player1.setTotalGamesPlayed(player1.getTotalGamesPlayed() + 1);
        player2.setTotalGamesPlayed(player2.getTotalGamesPlayed() + 1);

        if (!isDraw && winnerId != null) {
            if (winnerId.equals(player1.getId())) {
                player1.setTotalWins(player1.getTotalWins() + 1);
                // Winner's moves = total moves / 2, rounded up
                int winnerMoves = (int) Math.ceil(game.getMoveCount() / 2.0);
                player1.setTotalMovesMadeInWins(player1.getTotalMovesMadeInWins() + winnerMoves);
            } else {
                player2.setTotalWins(player2.getTotalWins() + 1);
                int winnerMoves = game.getMoveCount() / 2; // P2 moves
                if(game.getMoveCount() % 2 != 0) winnerMoves++; // P1 starts, so if move_count is odd, P1 made more
                // Re-check logic for move count. P1 moves = ceil(count/2). P2 moves = floor(count/2).
                int p1_moves = (int) Math.ceil(game.getMoveCount() / 2.0);
                int p2_moves = (int) Math.floor(game.getMoveCount() / 2.0);
                player2.setTotalMovesMadeInWins(player2.getTotalMovesMadeInWins() + p2_moves);

            }
        }
        
        userRepository.save(player1);
        userRepository.save(player2);
    }
}