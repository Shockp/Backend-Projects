# Number Guessing Game - Sequence Diagram

## Main Game Flow Sequence Diagram

```mermaid
sequenceDiagram
    participant User
    participant NumberGuessingCLI
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
    participant NumberGeneratorService

    Note over User, NumberGeneratorService: Application Initialization
    User->>NumberGuessingCLI: Start Application
    NumberGuessingCLI->>NumberGuessingCLI: initializeDependencies()
    NumberGuessingCLI->>NumberGeneratorService: Create NumberGeneratorService
    NumberGuessingCLI->>GameService: Create GameService(NumberGeneratorService)
    NumberGuessingCLI->>ConsoleView: Create ConsoleView
    NumberGuessingCLI->>InMemoryGameRepository: Create Repository
    NumberGuessingCLI->>StartGameUseCase: Create StartGameUseCase
    NumberGuessingCLI->>MakeGuessUseCase: Create MakeGuessUseCase
    NumberGuessingCLI->>EndGameUseCase: Create EndGameUseCase
    NumberGuessingCLI->>GameController: Create GameController
    NumberGuessingCLI->>GameController: startGame()
    
    Note over User, NumberGeneratorService: Game Setup
    GameController->>ConsoleView: displayWelcomeMessage()
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
    GameService->>NumberGeneratorService: generateNumber()
    NumberGeneratorService-->>GameService: Return random number
    GameService->>Game: Create Game(difficulty, player, targetNumber)
    GameService-->>StartGameUseCase: Return game instance
    StartGameUseCase->>InMemoryGameRepository: save(game)
    StartGameUseCase->>ConsoleView: displayMessage("Game started!")
    StartGameUseCase-->>GameController: Return game instance
    
    Note over User, NumberGeneratorService: Game Loop
    loop Until game ends
        GameController->>MakeGuessUseCase: execute(game)
        MakeGuessUseCase->>ConsoleView: getValidGuess(game)
        ConsoleView-->>User: Prompt for guess
        User-->>ConsoleView: Enter guess
        ConsoleView-->>MakeGuessUseCase: Return guess
        MakeGuessUseCase->>MakeGuessUseCase: validateGuess(guess)
        alt Valid guess
            MakeGuessUseCase->>GameService: processGuess(game, guess)
            GameService->>GameService: validateInput(game, guess)
            alt Game not started
                GameService->>Game: startGame()
            end
            GameService->>Game: recordGuess(guess)
            Game->>Game: Check guess vs target
            alt Correct guess
                Game->>Game: Set state to WON
                Game->>Player: incrementScore()
                Game-->>GameService: Return CORRECT
            else Wrong guess
                Game->>Game: Increment attempts
                alt Max attempts reached
                    Game->>Game: Set state to LOST
                    Game-->>GameService: Return GAME_OVER
                else Attempts remaining
                    Game-->>GameService: Return TOO_HIGH/TOO_LOW
                end
            end
            GameService->>GameService: generateFeedback(game, result, guess)
            GameService-->>MakeGuessUseCase: Return feedback
            MakeGuessUseCase->>ConsoleView: displayMessage(feedback)
            MakeGuessUseCase-->>GameController: Return feedback
            ConsoleView-->>User: Show feedback
            GameController->>GameController: handleGameState(game)
            GameController->>ConsoleView: displayGameState(game)
            ConsoleView-->>User: Show game status
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
            EndGameUseCase-->>GameController: Return boolean
            alt User wants to play again
                GameController->>StartGameUseCase: execute()
                Note over StartGameUseCase: Repeat game setup
            else User wants to exit
                GameController->>ConsoleView: displayGoodbyeMessage()
                ConsoleView-->>User: Show goodbye message
                GameController-->>NumberGuessingCLI: Exit game loop
            end
        end
    end
    
    NumberGuessingCLI-->>User: Application ends
```

## Use Case Sequence Diagrams

### Start Game Use Case

```mermaid
sequenceDiagram
    participant GameController
    participant StartGameUseCase
    participant ConsoleView
    participant GameService
    participant NumberGeneratorService
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
    GameService->>NumberGeneratorService: generateNumber()
    NumberGeneratorService-->>GameService: 42
    GameService->>Game: new Game(difficulty, player, 42)
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
        GameService->>GameService: validateInput(game, guess)
        alt Game not started
            GameService->>Game: startGame()
        end
        GameService->>Game: recordGuess(guess)
        Game->>Game: Check guess vs target
        alt Correct guess
            Game->>Game: Set state to WON
            Game->>Player: incrementScore()
            Game-->>GameService: CORRECT
        else Wrong guess
            Game->>Game: Increment attempts
            alt Max attempts reached
                Game->>Game: Set state to LOST
                Game-->>GameService: GAME_OVER
            else Attempts remaining
                Game-->>GameService: TOO_HIGH/TOO_LOW
            end
        end
        GameService->>GameService: generateFeedback(game, result, guess)
        GameService-->>MakeGuessUseCase: "Congratulations! You've guessed the number in X attempts."
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

    GameController->>EndGameUseCase: execute(game)
    EndGameUseCase->>InMemoryGameRepository: saveGameStatistics(game)
    EndGameUseCase->>ConsoleView: displayFinalResult(game)
    ConsoleView-->>User: Show final result
    EndGameUseCase->>ConsoleView: askToPlayAgain()
    ConsoleView-->>User: "Do you want to play again? (y/n)"
    User-->>ConsoleView: "y" or "n"
    ConsoleView-->>EndGameUseCase: Boolean choice
    EndGameUseCase-->>GameController: Boolean
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
- NumberGuessingCLI creates all dependencies with proper dependency injection
- GameService is created with NumberGeneratorService dependency
- GameController starts the main game loop

### 2. **Game Setup Flow**
- Player name input and validation
- Difficulty selection from menu
- Game creation through GameService with dependency injection
- Game persistence in repository

### 3. **Game Loop with Complete Architecture**
- Continuous guess processing until game ends
- Input validation through GameService
- Game state management with GuessResult enum
- User feedback and hints through rich domain service
- Real-time game status display

### 4. **Game Completion**
- Final result display
- Statistics saving
- Play again option
- Clean exit handling

### 5. **Error Handling**
- Invalid input validation through GameService
- Game state validation
- Exception handling throughout the flow
- Graceful error recovery

### 6. **Complete Implementation Features**
- **Dependency Injection**: GameService properly injects NumberGeneratorService
- **Separation of Concerns**: Game entity is pure, GameService handles business logic
- **Clean State Management**: GuessResult enum for clear state transitions
- **Rich Domain Service**: Centralized business logic and validation
- **Professional UI**: Comprehensive user feedback and status updates
- **Error Recovery**: Graceful handling of edge cases and invalid input

This sequence diagram shows the complete flow of the number guessing game, demonstrating how all components interact following the hexagonal architecture principles with complete implementation and enhanced user experience. 