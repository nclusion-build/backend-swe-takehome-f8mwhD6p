package com.takehome.gamengine.controller;

import com.takehome.gamengine.dto.CreateUserRequest;
import com.takehome.gamengine.dto.UserStatsDTO;
import com.takehome.gamengine.model.User;
import com.takehome.gamengine.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/users")
    public ResponseEntity<User> createUser(@RequestBody CreateUserRequest request) {
        User user = userService.createUser(request.getUsername());
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    @GetMapping("/users/{id}/stats")
    public ResponseEntity<UserStatsDTO> getUserStats(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserStats(id));
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<List<UserStatsDTO>> getLeaderboard(@RequestParam(defaultValue = "wins") String by) {
        return ResponseEntity.ok(userService.getLeaderboard(by));
    }
}