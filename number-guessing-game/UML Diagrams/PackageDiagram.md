# Number Guessing Game - Package Diagram

```mermaid
graph TB
    subgraph "Number Guessing Game"
        subgraph "Domain Layer"
            subgraph "com.shockp.numberguessinggame.domain.model"
                Game[Game.java]
                Player[Player.java]
                GameState[GameState.java]
            end
            
            subgraph "com.shockp.numberguessinggame.domain.model.difficulty"
                GameDifficulty[GameDifficulty.java]
                DifficultyStrategy[DifficultyStrategy.java]
                DifficultyEasy[DifficultyEasy.java]
                DifficultyMedium[DifficultyMedium.java]
                DifficultyHard[DifficultyHard.java]
            end
            
            subgraph "com.shockp.numberguessinggame.domain.service"
                GameService[GameService.java]
                NumberGeneratorService[NumberGeneratorService.java]
            end
        end
        
        subgraph "Application Layer"
            subgraph "com.shockp.numberguessinggame.application.port"
                GameRepository[GameRepository.java]
                UserInterface[UserInterface.java]
            end
            
            subgraph "com.shockp.numberguessinggame.application.usecase"
                StartGameUseCase[StartGameUseCase.java]
                MakeGuessUseCase[MakeGuessUseCase.java]
                EndGameUseCase[EndGameUseCase.java]
            end
        end
        
        subgraph "Infrastructure Layer"
            subgraph "com.shockp.numberguessinggame.infrastructure.persistence"
                InMemoryGameRepository[InMemoryGameRepository.java]
            end
            
            subgraph "com.shockp.numberguessinggame.infrastructure.cli"
                ConsoleView[ConsoleView.java]
                GameController[GameController.java]
            end
            
            subgraph "com.shockp.numberguessinggame.infrastructure.factory"
                GameFactory[GameFactory.java]
            end
        end
        
        subgraph "Main Application"
            Main[Main.java]
        end
        
        subgraph "Utility Classes"
            GameResult[GameResult.java]
            GameException[GameException.java]
            InputValidator[InputValidator.java]
        end
    end
    
    %% Dependencies between packages
    StartGameUseCase --> GameService
    StartGameUseCase --> GameRepository
    StartGameUseCase --> UserInterface
    StartGameUseCase --> Game
    StartGameUseCase --> GameDifficulty
    
    MakeGuessUseCase --> GameService
    MakeGuessUseCase --> UserInterface
    MakeGuessUseCase --> Game
    
    EndGameUseCase --> GameRepository
    EndGameUseCase --> UserInterface
    EndGameUseCase --> Game
    EndGameUseCase --> GameResult
    
    InMemoryGameRepository --> GameRepository
    InMemoryGameRepository --> Game
    
    ConsoleView --> UserInterface
    ConsoleView --> Game
    
    GameController --> StartGameUseCase
    GameController --> MakeGuessUseCase
    GameController --> EndGameUseCase
    GameController --> UserInterface
    GameController --> Game
    
    GameFactory --> Game
    GameFactory --> Player
    GameFactory --> GameDifficulty
    
    Main --> GameController
    Main --> GameService
    Main --> GameRepository
    Main --> UserInterface
    
    Game --> GameDifficulty
    Game --> Player
    Game --> GameState
    Game --> NumberGeneratorService
    
    GameDifficulty --> DifficultyStrategy
    DifficultyEasy --> DifficultyStrategy
    DifficultyMedium --> DifficultyStrategy
    DifficultyHard --> DifficultyStrategy
    
    GameException --> Exception
    
    InputValidator --> StartGameUseCase
    InputValidator --> MakeGuessUseCase
    InputValidator --> ConsoleView
    
    %% Styling
    classDef domainClass fill:#e1f5fe
    classDef applicationClass fill:#f3e5f5
    classDef infrastructureClass fill:#e8f5e8
    classDef mainClass fill:#fff3e0
    classDef utilityClass fill:#fce4ec
    
    class Game,Player,GameState,GameDifficulty,DifficultyStrategy,DifficultyEasy,DifficultyMedium,DifficultyHard,GameService,NumberGeneratorService domainClass
    class GameRepository,UserInterface,StartGameUseCase,MakeGuessUseCase,EndGameUseCase applicationClass
    class InMemoryGameRepository,ConsoleView,GameController,GameFactory infrastructureClass
    class Main mainClass
    class GameResult,GameException,InputValidator utilityClass
```

## Package Structure Overview

### Domain Layer
**Purpose**: Contains the core business logic and domain entities

#### `com.shockp.numberguessinggame.domain.model`
- **Game.java**: Main game entity with business rules
- **Player.java**: Player entity with score management
- **GameState.java**: Enumeration of game states

#### `com.shockp.numberguessinggame.domain.model.difficulty`
- **GameDifficulty.java**: Strategy pattern wrapper for difficulty levels
- **DifficultyStrategy.java**: Interface for difficulty implementations
- **DifficultyEasy.java**: Easy difficulty implementation (10 attempts)
- **DifficultyMedium.java**: Medium difficulty implementation (5 attempts)
- **DifficultyHard.java**: Hard difficulty implementation (3 attempts)

#### `com.shockp.numberguessinggame.domain.service`
- **GameService.java**: High-level game operations
- **NumberGeneratorService.java**: Random number generation service

### Application Layer
**Purpose**: Contains application business logic and port interfaces

#### `com.shockp.numberguessinggame.application.port`
- **GameRepository.java**: Data persistence contract (port)
- **UserInterface.java**: User interaction contract (port)

#### `com.shockp.numberguessinggame.application.usecase`
- **StartGameUseCase.java**: Game initialization logic
- **MakeGuessUseCase.java**: Guess processing logic
- **EndGameUseCase.java**: Game completion logic

### Infrastructure Layer
**Purpose**: Contains implementations of ports and external concerns

#### `com.shockp.numberguessinggame.infrastructure.persistence`
- **InMemoryGameRepository.java**: In-memory implementation of GameRepository

#### `com.shockp.numberguessinggame.infrastructure.cli`
- **ConsoleView.java**: Command-line implementation of UserInterface
- **GameController.java**: Application flow orchestration

#### `com.shockp.numberguessinggame.infrastructure.factory`
- **GameFactory.java**: Factory for creating domain objects

### Main Application
- **Main.java**: Application entry point and dependency setup

### Utility Classes
- **GameResult.java**: Result data transfer object
- **GameException.java**: Custom exception handling
- **InputValidator.java**: Input validation utilities

## Architecture Principles

### Hexagonal Architecture (Ports and Adapters)
- **Ports**: Interfaces in the application layer (`GameRepository`, `UserInterface`)
- **Adapters**: Implementations in the infrastructure layer
- **Domain**: Core business logic isolated from external concerns

### Dependency Direction
- Domain layer has no dependencies on other layers
- Application layer depends only on domain layer
- Infrastructure layer depends on application layer (implements ports)
- Main application orchestrates all layers

### Package Dependencies
- **Domain → No dependencies**: Pure business logic
- **Application → Domain**: Uses domain services and entities
- **Infrastructure → Application**: Implements application ports
- **Main → All layers**: Coordinates and wires dependencies 