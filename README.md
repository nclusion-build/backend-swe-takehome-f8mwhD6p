# Backend SWE Take-Home Assignment - Java

## Overview

This is a **3-4 hour take-home assignment**. You will build a small, network-accessible backend web service that manages a turn-based, grid-driven game from pre-defined rules. Your assignment is tailored: a randomized (but reproducible) set of TODOs, features, and bugs has been embedded inline.

You should focus on:
- Clear, maintainable API handlers and service logic
- Robust input validation and error handling
- Simple, reliable tests (unit and integration)
- Helpful logs/metrics stubs where applicable

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6 or higher

### Installation

```bash
mvn clean install
```

### Running the Application

```bash
mvn spring-boot:run
```

The application will start on port 8080.

### Running Tests

```bash
mvn test
```

### Running the Simulation

> Optional: You may create a simple simulation script or test that spins up your server, plays multiple sessions concurrently, and prints a small leaderboard summary.

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── example/
│   │           ├── Application.java
│   │           ├── controller/
│   │           ├── model/
│   │           ├── service/
│   │           └── repository/
│   └── resources/
│       └── application.properties
└── test/
    └── java/
        └── com/
            └── example/
                ├── controller/
                ├── model/
                └── service/
```

## What You Need to Implement

### Selected Tasks

#### TODOs
- Validate Player model (name/email uniqueness, format)
- Add player email validation
- Harden route validation for IDs and payloads
- Complete players routes (update, delete, search)
- Add unit tests for GameModel and PlayerModel
- Implement and validate core game logic (join/move/win/draw)
- Add request logging middleware
- Standardize service error handling and messages
- Implement PlayerService (create/get/update/delete/search/stats)

#### Feature Requests
- Add basic rate limiting middleware
- Implement basic leaderboard endpoint

#### Bugs To Fix

### Core Requirements (high-level)

1. Turn-based rules on a finite grid with obvious invalid-move conditions
2. Multiple sessions can run concurrently; two players start a session
3. End a session on win or draw; expose session status
4. Leaderboard endpoint returning top users by wins or "efficiency" (lower moves per win is better)
5. A small simulation or test path that exercises the API

Additionally, look for inline TODOs in language-appropriate files. Examples:
- Java: `src/main/java/com/example/controller/*`, `src/main/java/com/example/service/*`, `src/main/java/com/example/model/*`, `src/main/java/com/example/Application.java`

> Focus on correctness, quality, and clarity. If you finish early, feel free to polish or extend.

## Notes

- Inline TODOs are your primary guide. GitHub Issues are intentionally disabled.
- Keep commits small and frequent with clear messages.
- You may add libraries if they help you implement tasks cleanly.

## Quick API Examples

Assuming your server is running on http://localhost:8080

Create a game
```bash
curl -s -X POST http://localhost:8080/games -H 'Content-Type: application/json' -d '{"name":"Sample"}' | jq .
```

Join the game
```bash
GAME_ID=<paste-from-create>
curl -s -X POST http://localhost:8080/games/$GAME_ID/join -H 'Content-Type: application/json' -d '{"playerId":"player-1"}' | jq .
curl -s -X POST http://localhost:8080/games/$GAME_ID/join -H 'Content-Type: application/json' -d '{"playerId":"player-2"}' | jq .
```

Make a move and get status
```bash
curl -s -X POST http://localhost:8080/games/$GAME_ID/moves -H 'Content-Type: application/json' -d '{"playerId":"player-1","row":0,"col":0}' | jq .
curl -s http://localhost:8080/games/$GAME_ID/status | jq .
```

Leaderboard (optional)
```bash
curl -s http://localhost:8080/leaderboard | jq .
```

## Submission

1. Ensure tests pass
2. Run the simulation script
3. Update this README with any setup notes
4. Submit your repository URL

Good luck! 🚀
