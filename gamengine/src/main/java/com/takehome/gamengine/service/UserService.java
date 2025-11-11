package com.takehome.gamengine.service;

import com.takehome.gamengine.dto.UserStatsDTO;
import com.takehome.gamengine.model.User;
import com.takehome.gamengine.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User createUser(String username) {
        // Note: In a real app, check for username uniqueness
        User user = new User(username);
        return userRepository.save(user);
    }

    public UserStatsDTO getUserStats(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return new UserStatsDTO(user);
    }

    public List<UserStatsDTO> getLeaderboard(String by) {
        List<User> users;
        if ("wins".equalsIgnoreCase(by)) {
            users = userRepository.findTop3ByOrderByTotalWinsDesc();
        } else if ("efficiency".equalsIgnoreCase(by)) {
            users = userRepository.findTop3ByEfficiency();
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid 'by' parameter. Use 'wins' or 'efficiency'.");
        }
        
        return users.stream()
                    .map(UserStatsDTO::new)
                    .collect(Collectors.toList());
    }
}