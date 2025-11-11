package com.takehome.gamengine.dto;

import com.takehome.gamengine.model.User;
import lombok.Data;

@Data
public class UserStatsDTO {
    private Long id;
    private String username;
    private int totalWins;
    private int totalGamesPlayed;
    private double winRatio;
    private Double efficiency; // Can be null if no wins

    public UserStatsDTO(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.totalWins = user.getTotalWins();
        this.totalGamesPlayed = user.getTotalGamesPlayed();
        
        this.winRatio = (user.getTotalGamesPlayed() > 0) 
            ? (double) user.getTotalWins() / user.getTotalGamesPlayed() 
            : 0.0;
        
        this.efficiency = (user.getTotalWins() > 0)
            ? (double) user.getTotalMovesMadeInWins() / user.getTotalWins()
            : null;
    }
}