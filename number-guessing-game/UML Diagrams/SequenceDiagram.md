# Number Guessing Game - Sequence Diagram

## Main Game Flow Sequence Diagram

```mermaid
sequenceDiagram
    participant User
    participant Main
    participant GameController
    participant StartGameUseCase
    participant MakeGuessUseCase
    participant EndGameUseCase
    participant ConsoleView
    participant GameService
    participant Game
    participant InMemoryGameRepository
    participant Player
    participant GameDifficulty

    Note over User, GameDifficulty: Game Initialization
    User->>Main: Start Application
    Main->>Main: initializeDependencies()
    Main->>GameController: Create GameController
    Main->>ConsoleView: Create ConsoleView
    Main->>InMemoryGameRepository: Create Repository
    Main->>GameService: Create GameService
    Main->>StartGameUseCase: Create StartGameUseCase
    Main->>MakeGuessUseCase: Create MakeGuessUseCase
    Main->>EndGameUseCase: Create EndGameUseCase
    Main->>GameController: startGame()
    
    Note over User, GameDifficulty: Game Setup
    GameController->>ConsoleView: displayWelcome()
    ConsoleView-->>User: Show welcome message
    GameController->>StartGameUseCase: execute()
    StartGameUseCase->>ConsoleView: getPlayerName()
    ConsoleView-->>User: Prompt for player name
    User-->>ConsoleView: Enter player name
    ConsoleView-->>StartGameUseCase: Return player name
    StartGameUseCase->>StartGameUseCase: selectDifficulty()
    StartGameUseCase->>ConsoleView: displayMenu()
    ConsoleView-->>User: Show difficulty options
    User-->>ConsoleView: Select difficulty
    ConsoleView-->>StartGameUseCase: Return difficulty choice
    StartGameUseCase->>GameDifficulty: Create difficulty
    StartGameUseCase->>Player: Create player
    StartGameUseCase->>GameService: createGame(difficulty, player)
    GameService->>Game: Create Game(difficulty, player)
    Game->>Game: Generate target number
    GameService-->>StartGameUseCase: Return game instance
    StartGameUseCase->>InMemoryGameRepository: save(game)
    StartGameUseCase->>ConsoleView: displayMessage("Game started!")
    StartGameUseCase-->>GameController: Return game instance
    
    Note over User, GameDifficulty: Game Loop
    loop Until game ends
        GameController->>MakeGuessUseCase: execute(game)
        MakeGuessUseCase->>ConsoleView: getValidGuess(game)
        ConsoleView-->>User: Prompt for guess
        User-->>ConsoleView: Enter guess
        ConsoleView-->>MakeGuessUseCase: Return guess
        MakeGuessUseCase->>MakeGuessUseCase: validateGuess(guess)
        alt Valid guess
            MakeGuessUseCase->>GameService: processGuess(game, guess)
            GameService->>Game: makeGuess(guess)
            Game->>Game: Check guess vs target
            alt Correct guess
                Game->>Game: Set state to WON
                Game->>Player: incrementScore()
                Game-->>GameService: Return win message
            else Wrong guess
                Game->>Game: Increment attempts
                alt Max attempts reached
                    Game->>Game: Set state to LOST
                    Game-->>GameService: Return lose message
                else Attempts remaining
                    Game-->>GameService: Return hint message
                end
            end
            GameService-->>MakeGuessUseCase: Return feedback
            MakeGuessUseCase->>ConsoleView: displayMessage(feedback)
            MakeGuessUseCase-->>GameController: Return feedback
            ConsoleView-->>User: Show feedback
        else Invalid guess
            MakeGuessUseCase->>ConsoleView: displayError("Invalid input")
            MakeGuessUseCase-->>GameController: Return error
            ConsoleView-->>User: Show error message
        end
        
        alt Game is over
            GameController->>EndGameUseCase: execute(game)
            EndGameUseCase->>InMemoryGameRepository: saveGameStatistics(game)
            EndGameUseCase->>ConsoleView: displayFinalResult(game)
            ConsoleView-->>User: Show final result
            EndGameUseCase->>ConsoleView: askToPlayAgain()
            ConsoleView-->>User: Ask to play again
            User-->>ConsoleView: Yes/No response
            ConsoleView-->>EndGameUseCase: Return choice
            EndGameUseCase-->>GameController: Return GameResult
            alt User wants to play again
                GameController->>StartGameUseCase: execute()
                Note over StartGameUseCase: Repeat game setup
            else User wants to exit
                GameController->>ConsoleView: displayGoodbye()
                ConsoleView-->>User: Show goodbye message
                GameController-->>Main: Exit game loop
            end
        end
    end
    
    Main-->>User: Application ends
```

## Use Case Sequence Diagrams

### Start Game Use Case

```mermaid
sequenceDiagram
    participant GameController
    participant StartGameUseCase
    participant ConsoleView
    participant GameService
    participant Game
    participant Player
    participant GameDifficulty
    participant InMemoryGameRepository

    GameController->>StartGameUseCase: execute()
    StartGameUseCase->>ConsoleView: getPlayerName()
    ConsoleView-->>StartGameUseCase: "Player Name"
    StartGameUseCase->>StartGameUseCase: selectDifficulty()
    StartGameUseCase->>ConsoleView: displayMenu()
    ConsoleView-->>StartGameUseCase: "Medium"
    StartGameUseCase->>GameDifficulty: medium()
    GameDifficulty-->>StartGameUseCase: GameDifficulty instance
    StartGameUseCase->>Player: new Player("Player Name")
    Player-->>StartGameUseCase: Player instance
    StartGameUseCase->>GameService: createGame(difficulty, player)
    GameService->>Game: new Game(difficulty, player)
    Game-->>GameService: Game instance
    GameService-->>StartGameUseCase: Game instance
    StartGameUseCase->>InMemoryGameRepository: save(game)
    StartGameUseCase->>ConsoleView: displayMessage("Game started!")
    StartGameUseCase-->>GameController: Game instance
```

### Make Guess Use Case

```mermaid
sequenceDiagram
    participant GameController
    participant MakeGuessUseCase
    participant ConsoleView
    participant GameService
    participant Game
    participant Player

    GameController->>MakeGuessUseCase: execute(game, guess)
    MakeGuessUseCase->>MakeGuessUseCase: validateGuess(guess)
    alt Valid guess
        MakeGuessUseCase->>GameService: processGuess(game, guess)
        GameService->>Game: makeGuess(guess)
        Game->>Game: Check guess vs target
        alt Correct guess
            Game->>Game: Set state to WON
            Game->>Player: incrementScore()
            Game-->>GameService: "Congratulations! You've guessed the number in X attempts."
        else Wrong guess
            Game->>Game: Increment attempts
            alt Max attempts reached
                Game->>Game: Set state to LOST
                Game-->>GameService: "Sorry, you've run out of attempts. The number was X."
            else Attempts remaining
                Game-->>GameService: "Too high/low! Try again. You have X attempts left."
            end
        end
        GameService-->>MakeGuessUseCase: Feedback message
        MakeGuessUseCase->>ConsoleView: displayMessage(feedback)
        MakeGuessUseCase-->>GameController: Feedback message
    else Invalid guess
        MakeGuessUseCase->>ConsoleView: displayError("Invalid input")
        MakeGuessUseCase-->>GameController: Error message
    end
```

### End Game Use Case

```mermaid
sequenceDiagram
    participant GameController
    participant EndGameUseCase
    participant ConsoleView
    participant InMemoryGameRepository
    participant Game
    participant GameResult

    GameController->>EndGameUseCase: execute(game)
    EndGameUseCase->>InMemoryGameRepository: saveGameStatistics(game)
    EndGameUseCase->>ConsoleView: displayFinalResult(game)
    ConsoleView-->>User: Show final result
    EndGameUseCase->>GameResult: new GameResult(won, attempts, maxAttempts, playerName, difficulty)
    GameResult-->>EndGameUseCase: GameResult instance
    EndGameUseCase->>ConsoleView: askToPlayAgain()
    ConsoleView-->>User: "Do you want to play again? (y/n)"
    User-->>ConsoleView: "y" or "n"
    ConsoleView-->>EndGameUseCase: Boolean choice
    EndGameUseCase-->>GameController: GameResult
```

## Repository Operations Sequence Diagram

```mermaid
sequenceDiagram
    participant UseCase
    participant InMemoryGameRepository
    participant Game
    participant Map

    Note over UseCase, Map: Save Game
    UseCase->>InMemoryGameRepository: save(game)
    InMemoryGameRepository->>InMemoryGameRepository: generateGameId()
    InMemoryGameRepository->>Map: put(gameId, game)
    InMemoryGameRepository-->>UseCase: void

    Note over UseCase, Map: Load Game
    UseCase->>InMemoryGameRepository: load(gameId)
    InMemoryGameRepository->>Map: get(gameId)
    alt Game exists
        Map-->>InMemoryGameRepository: Game instance
        InMemoryGameRepository-->>UseCase: Game instance
    else Game not found
        Map-->>InMemoryGameRepository: null
        InMemoryGameRepository-->>UseCase: null
    end

    Note over UseCase, Map: Delete Game
    UseCase->>InMemoryGameRepository: delete(gameId)
    InMemoryGameRepository->>Map: remove(gameId)
    InMemoryGameRepository-->>UseCase: void
```

## Key Interactions Explained

### 1. **Application Initialization**
- Main class creates all dependencies
- Dependency injection setup
- GameController starts the main game loop

### 2. **Game Setup Flow**
- Player name input and validation
- Difficulty selection from menu
- Game creation with domain services
- Game persistence in repository

### 3. **Game Loop**
- Continuous guess processing until game ends
- Input validation and error handling
- Game state management
- User feedback and hints

### 4. **Game Completion**
- Final result display
- Statistics saving
- Play again option
- Clean exit handling

### 5. **Error Handling**
- Invalid input validation
- Game state validation
- Exception handling throughout the flow

This sequence diagram shows the complete flow of the number guessing game, demonstrating how all components interact following the hexagonal architecture principles. 