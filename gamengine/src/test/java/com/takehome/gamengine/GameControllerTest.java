package com.takehome.gamengine;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testCreateUser() throws Exception {
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\": \"testuser\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    public void testCreateAndJoinGame() throws Exception {
        // 1. Create players
        String user1Json = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\": \"player1\"}")).andReturn().getResponse().getContentAsString();
        String user2Json = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\": \"player2\"}")).andReturn().getResponse().getContentAsString();
        
        long player1Id = Long.parseLong(com.jayway.jsonpath.JsonPath.read(user1Json, "$.id").toString());
        long player2Id = Long.parseLong(com.jayway.jsonpath.JsonPath.read(user2Json, "$.id").toString());

        // 2. Player 1 creates game
        String gameJson = mockMvc.perform(post("/api/game/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"player1_id\": " + player1Id + "}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        
        String gameId = com.jayway.jsonpath.JsonPath.read(gameJson, "$.id");

        // 3. Player 2 joins game
        mockMvc.perform(post("/api/game/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"gameId\": \"" + gameId + "\", \"player2Id\": " + player2Id + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.currentTurnPlayerId").value(player1Id));
    }

    @Test
    public void testFullGame_Player1Wins() throws Exception {
        // 1. Create players
        String user1Json = mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content("{\"username\": \"player1\"}")).andReturn().getResponse().getContentAsString();
        String user2Json = mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content("{\"username\": \"player2\"}")).andReturn().getResponse().getContentAsString();
        long p1Id = Long.parseLong(com.jayway.jsonpath.JsonPath.read(user1Json, "$.id").toString());
        long p2Id = Long.parseLong(com.jayway.jsonpath.JsonPath.read(user2Json, "$.id").toString());

        // 2. Create and join game
        String gameJson = mockMvc.perform(post("/api/game/create").contentType(MediaType.APPLICATION_JSON).content("{\"player1_id\": " + p1Id + "}")).andReturn().getResponse().getContentAsString();
        String gameId = com.jayway.jsonpath.JsonPath.read(gameJson, "$.id");
        mockMvc.perform(post("/api/game/join").contentType(MediaType.APPLICATION_JSON).content("{\"gameId\": \"" + gameId + "\", \"player2Id\": " + p2Id + "}"));

        // 3. Play game
        // P1: (0,0)
        mockMvc.perform(post("/api/game/move").contentType(MediaType.APPLICATION_JSON).content(String.format("{\"gameId\": \"%s\", \"playerId\": %d, \"row\": 0, \"col\": 0}", gameId, p1Id))).andExpect(status().isOk());
        // P2: (1,0)
        mockMvc.perform(post("/api/game/move").contentType(MediaType.APPLICATION_JSON).content(String.format("{\"gameId\": \"%s\", \"playerId\": %d, \"row\": 1, \"col\": 0}", gameId, p2Id))).andExpect(status().isOk());
        // P1: (0,1)
        mockMvc.perform(post("/api/game/move").contentType(MediaType.APPLICATION_JSON).content(String.format("{\"gameId\": \"%s\", \"playerId\": %d, \"row\": 0, \"col\": 1}", gameId, p1Id))).andExpect(status().isOk());
        // P2: (1,1)
        mockMvc.perform(post("/api/game/move").contentType(MediaType.APPLICATION_JSON).content(String.format("{\"gameId\": \"%s\", \"playerId\": %d, \"row\": 1, \"col\": 1}", gameId, p2Id))).andExpect(status().isOk());
        // P1: (0,2) -> WINS
        mockMvc.perform(post("/api/game/move").contentType(MediaType.APPLICATION_JSON).content(String.format("{\"gameId\": \"%s\", \"playerId\": %d, \"row\": 0, \"col\": 2}", gameId, p1Id)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FINISHED"))
                .andExpect(jsonPath("$.winnerId").value(p1Id));
        
        // 4. Check leaderboard
        mockMvc.perform(get("/api/leaderboard?by=wins"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("player1"))
                .andExpect(jsonPath("$[0].totalWins").value(1));
    }
}