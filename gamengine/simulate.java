// Standalone Java 11+ Simulation Script
// To run: 
// 1. Make sure the Spring Boot app is running
// 2. Run this file: `java simulate.java`

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class simulate {

    private static final String BASE_URL = "http://localhost:8080/api";
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // Main simulation method
    public static void main(String[] args) throws Exception {
        int numPlayers = 10;
        int numGames = 50;

        System.out.println("--- Starting Game Simulation ---");
        System.out.println("Creating " + numPlayers + " players...");
        
        List<Long> playerIds = new ArrayList<>();
        for (int i = 1; i <= numPlayers; i++) {
            try {
                String userJson = sendRequest(
                    "POST", 
                    "/users", 
                    "{\"username\": \"SimPlayer" + i + "\"}"
                );
                Map<String, Object> user = objectMapper.readValue(userJson, new TypeReference<Map<String, Object>>(){});
                playerIds.add(((Number)user.get("id")).longValue());
            } catch (Exception e) {
                System.out.println("Could not create user. Is server running? " + e.getMessage());
                return;
            }
        }
        System.out.println(playerIds.size() + " players created.");

        // Run games concurrently
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
        for (int i = 0; i < numGames; i++) {
            executor.submit(() -> {
                try {
                    // Pick two random, different players
                    Random rand = new Random();
                    Long p1 = playerIds.get(rand.nextInt(playerIds.size()));
                    Long p2 = p1;
                    while (p2.equals(p1)) {
                        p2 = playerIds.get(rand.nextInt(playerIds.size()));
                    }
                    playGame(p1, p2);
                } catch (Exception e) {
                    System.err.println("Game simulation failed: " + e.getMessage());
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.MINUTES);

        System.out.println("\n--- Simulation Complete ---");
        System.out.println("Fetching final leaderboard...");

        // Get stats and calculate win ratio
        List<Map<String, Object>> allStats = new ArrayList<>();
        for (Long id : playerIds) {
            String statsJson = sendRequest("GET", "/users/" + id + "/stats", null);
            Map<String, Object> stats = objectMapper.readValue(statsJson, new TypeReference<Map<String, Object>>(){});
            allStats.add(stats);
        }

        // Sort by win ratio
        allStats.sort((s1, s2) -> {
            Double r1 = (Double) s1.get("winRatio");
            Double r2 = (Double) s2.get("winRatio");
            return r2.compareTo(r1); // Correctly returns an integer
        });

        System.out.println("\n--- Top 3 Players by Win Ratio ---");
        for (int i = 0; i < 3 && i < allStats.size(); i++) {
            Map<String, Object> user = allStats.get(i);
            System.out.printf("#%d: %s (ID: %d)\n", (i + 1), user.get("username"), user.get("id"));
            System.out.printf("  Win Ratio: %.2f%%\n", (Double) user.get("winRatio") * 100);
            System.out.printf("  Wins: %d / Games: %d\n", user.get("totalWins"), user.get("totalGamesPlayed"));
        }
    }

    private static void playGame(Long p1, Long p2) throws Exception {
        // 1. Create Game
        String createJson = sendRequest("POST", "/game/create", "{\"player1_id\": " + p1 + "}");
        Map<String, Object> game = objectMapper.readValue(createJson, new TypeReference<Map<String, Object>>(){});
        String gameId = (String) game.get("id");

        // 2. Join Game
        sendRequest("POST", "/game/join", "{\"gameId\": \"" + gameId + "\", \"player2Id\": " + p2 + "}");

        // 3. Play Game
        String gameStatus = "IN_PROGRESS";
        while ("IN_PROGRESS".equals(gameStatus)) {
            // Get current state
            String gameJson = sendRequest("GET", "/game/" + gameId, null);
            game = objectMapper.readValue(gameJson, new TypeReference<Map<String, Object>>(){});
            gameStatus = (String) game.get("status");
            if (!"IN_PROGRESS".equals(gameStatus)) break;

            Long currentPlayerId = ((Number) game.get("currentTurnPlayerId")).longValue();
            List<Object> grid = (List<Object>) game.get("grid");

            // Find a valid move
            List<Integer> validMoves = new ArrayList<>();
            for (int i = 0; i < 9; i++) {
                if (grid.get(i) == null) {
                    validMoves.add(i);
                }
            }
            
            if (validMoves.isEmpty()) break; // Should be a draw

            int move = validMoves.get(new Random().nextInt(validMoves.size()));
            int row = move / 3;
            int col = move % 3;

            // Make the move
            String moveJson = String.format("{\"gameId\": \"%s\", \"playerId\": %d, \"row\": %d, \"col\": %d}", 
                                            gameId, currentPlayerId, row, col);
            sendRequest("POST", "/game/move", moveJson);
        }
    }

    // Helper to send HTTP requests
    private static String sendRequest(String method, String endpoint, String body) throws Exception {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Content-Type", "application/json");

        if ("POST".equals(method)) {
            requestBuilder.POST(HttpRequest.BodyPublishers.ofString(body));
        } else {
            requestBuilder.GET();
        }
        
        HttpResponse<String> response = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("Failed: HTTP " + response.statusCode() + " " + response.body());
        }
        return response.body();
    }
}
