# BE-001-T3: Backend Systems Challenge

This project implements a backend API for a grid-based scoring game (Tic-Tac-Toe) using Java, Spring Boot, and an H2 in-memory SQL database.

## Technical Stack
- **Java 21**
- **Spring Boot 3**
- **Spring Data JPA** (for SQL modeling)
- **H2 (In-Memory SQL DB)** (for rapid setup)
- **Maven** (for dependency management)
- **JUnit 5** (for testing)
- **`java.net.http.HttpClient`** (for simulation script)

## Core Design Decisions
- **SQL Data Modeling:** Uses JPA Entities (`User`, `GameSession`) to model state. The 3x3 grid is stored as a JSON string in the `GameSession` table.
- **Concurrency:** The most critical concurrent action is two players trying to make a move at the same time. This is handled by using a **Pessimistic Write Lock** (`@Lock(LockModeType.PESSIMISTIC_WRITE)`) on the `GameSession` row during the `makeMove` transaction. This ensures that moves are processed atomically and prevents race conditions.
- **Error Handling:** Uses Spring Boot's `@RestControllerAdvice` (implicitly) and `ResponseStatusException` to return clear, standard HTTP error codes (e.g., 400 for bad request, 404 for not found).
- **Testability:** A `GameService` layer contains all business logic, making the `GameController` thin and easy to test.

---

## How to Run the API

1.  **Prerequisites:**
    * Java JDK 21
    * Apache Maven

2.  **Build the project:**
    ```bash
    ./mvnw clean install
    ```

3.  **Run the application:**
    ```bash
    java -jar target/gamengine-0.0.1-SNAPSHOT.jar
    ```
    The API will be running at `http://localhost:8080`.

4.  **Verify the API is Running:**
    * Note: Visiting `http://localhost:8080/` in your browser will show a `404 "Not Found" Whitelabel` Error Page. This is normal. The API only responds to specific paths under `/api`.
    * While the app is running, navigate to `http://localhost:8080/h2-console`
    * Enter the JDBC URL: `jdbc:h2:mem:gamedb`
    * Username: `ac`
    * Password: (leave blank)
    * Click "Connect" to browse the `USERS` and `GAME_SESSION` tables.

---

## How to Run the Tests

Run the included JUnit test suite:
```bash
./mvnw test
```

---

## How to Run the Simulation

1.  Make sure the API is running in one terminal.
2.  Open a **second terminal** in the same project root directory.
3.  Compile and run the standalone `simulate.java` file (it uses the dependencies from the `pom.xml`):
    ```bash
    # Compile the simulation
    ./mvnw dependency:build-classpath -Dmdep.outputFile=cp.txt
    javac -cp "target/classes:$(cat cp.txt)" simulate.java
    
    # Run the simulation
    java -cp ".:target/classes:$(cat cp.txt)" simulate
    ```
    *(Note: On Windows, use `java -cp ".;target/classes;$(cat cp.txt)" simulate`)*

This script will create 10 players, simulate 50 concurrent games, and then output the top 3 players by win ratio.
