package com.takehome.gamengine.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users") // "user" is a reserved SQL keyword
@Data // Lombok: auto-generates getters, setters, toString, etc.
@NoArgsConstructor // Lombok: creates a no-argument constructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    
    // Stats for Leaderboard
    private int totalWins = 0;
    private int totalGamesPlayed = 0;
    
    // Total moves made by this player *in games they won*
    private int totalMovesMadeInWins = 0;

    public User(String username) {
        this.username = username;
    }
}
