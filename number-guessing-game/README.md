# Number Guessing Game CLI
https://roadmap.sh/projects/number-guessing-game

A comprehensive command-line interface application built in Java that implements a number guessing game following hexagonal architecture principles. This application demonstrates modern Java development practices using clean architecture, dependency injection, and robust domain-driven design.

## Features
- **Multiple Difficulty Levels**: Choose from Easy (10 attempts), Medium (5 attempts), or Hard (3 attempts)
- **Player Management**: Track player names and scores with validation
- **Real-time Feedback**: Get immediate feedback on guesses with helpful hints
- **Game State Management**: Comprehensive state tracking with proper transitions
- **Score Tracking**: Automatic score increment for successful games
- **Play Again Functionality**: Seamless game restart with option to play multiple rounds
- **Input Validation**: Comprehensive validation for all user inputs with clear error messages
- **Professional UI**: Clean, user-friendly interface with status displays and menus
- **Error Handling**: Graceful handling of invalid input and edge cases
- **Modular Architecture**: Clean separation between domain, application, and infrastructure layers
- **Dependency Injection**: Constructor-based dependency management for testability
- **Hexagonal Architecture**: Clear separation of concerns with ports and adapters

## Prerequisites
- **Java Development Kit (JDK)**: Version 17 or higher
- **Maven**: Version 3.6 or higher for dependency management and building
- **IDE**: IntelliJ IDEA, Eclipse, or Visual Studio Code (optional but recommended)

### Installing Maven
If you don't have Maven installed:

1. **Download Maven**: Visit [Maven Official Site](https://maven.apache.org/download.cgi)
2. **Extract**: Extract to a directory (e.g., `C:\Program Files\Apache\maven`)
3. **Set Environment Variables**:
   - Add `MAVEN_HOME` pointing to Maven directory
   - Add `%MAVEN_HOME%\bin` to your `PATH`
4. **Verify Installation**: Run `mvn -version`

**Alternative**: Use your IDE's built-in Maven support (IntelliJ IDEA, Eclipse, VS Code)

## Quick Start
1. Clone or download the project with the Maven structure
2. Ensure you have Java 17+ and Maven 3.6+ installed
3. Navigate to the project directory
4. Run the application using one of the methods below
5. Enjoy the game!

## Installation

### 1. Clone the Repository
```bash
git clone 
cd number-guessing-game
```

### 2. Build the Project
```bash
mvn clean compile
```

### 3. Package the Application (Optional)
```bash
mvn clean package
```

## Usage

### Basic Gameplay
```bash
# Method 1: Run with Maven (Recommended)
mvn exec:java -Dexec.mainClass="com.shockp.numberguessinggame.NumberGuessingCLI"

# Method 2: Compile and run manually
mvn clean compile
java -cp "target/classes" com.shockp.numberguessinggame.NumberGuessingCLI

# Method 3: Package and run JAR (Alternative)
mvn clean package
java -jar target/number-guessing-game-1.0.0.jar

# Method 4: Direct compilation without Maven (Advanced)
javac -d target/classes -cp "src/main/java" src/main/java/com/shockp/numberguessinggame/*.java src/main/java/com/shockp/numberguessinggame/*/*.java src/main/java/com/shockp/numberguessinggame/*/*/*.java
java -cp "target/classes" com.shockp.numberguessinggame.NumberGuessingCLI
```

### Game Flow
1. **Welcome Screen**: Displays game introduction and instructions
2. **Player Setup**: Enter your name (validated for non-empty input)
3. **Difficulty Selection**: Choose from Easy, Medium, or Hard difficulty levels
4. **Gameplay**: Make guesses between 1-100 with real-time feedback
5. **Game Completion**: View final results and statistics
6. **Play Again**: Option to start a new game or exit

### Available Commands
- **help/h**: Display available commands and menu options
- **rules/r**: Show game rules and instructions
- **quit/exit/q**: Exit the game gracefully

## Difficulty Levels

### Easy Mode
- **Attempts**: 10 attempts to guess the number
- **Best for**: Beginners and casual players
- **Strategy**: Allows for systematic guessing and learning

### Medium Mode
- **Attempts**: 5 attempts to guess the number
- **Best for**: Intermediate players
- **Strategy**: Requires more strategic thinking and efficient guessing

### Hard Mode
- **Attempts**: 3 attempts to guess the number
- **Best for**: Advanced players and challenge seekers
- **Strategy**: Demands optimal guessing strategy and quick thinking

## Project Structure
```
number-guessing-game/
├── src/
│   ├── main/
│   │   ├── java/com/shockp/numberguessinggame/
│   │   │   ├── NumberGuessingCLI.java                    # Main application entry point
│   │   │   ├── application/                              # Application layer
│   │   │   │   ├── port/                                 # Port interfaces
│   │   │   │   │   ├── GameRepository.java              # Data persistence contract
│   │   │   │   │   └── UserInterface.java               # User interaction contract
│   │   │   │   └── usecase/                             # Application business logic
│   │   │   │       ├── StartGameUseCase.java            # Game initialization
│   │   │   │       ├── MakeGuessUseCase.java            # Guess processing
│   │   │   │       └── EndGameUseCase.java              # Game completion
│   │   │   ├── domain/                                  # Domain layer (core business logic)
│   │   │   │   ├── model/                               # Domain entities
│   │   │   │   │   ├── Game.java                       # Pure domain entity
│   │   │   │   │   ├── Player.java                     # Player entity
│   │   │   │   │   ├── GameState.java                  # Game state enumeration
│   │   │   │   │   └── difficulty/                     # Difficulty strategy pattern
│   │   │   │   │       ├── GameDifficulty.java         # Strategy wrapper
│   │   │   │   │       ├── DifficultyStrategy.java     # Strategy interface
│   │   │   │   │       ├── DifficultyEasy.java         # Easy implementation
│   │   │   │   │       ├── DifficultyMedium.java       # Medium implementation
│   │   │   │   │       └── DifficultyHard.java         # Hard implementation
│   │   │   │   └── service/                            # Domain services
│   │   │   │       ├── GameService.java                # Rich domain service
│   │   │   │       └── NumberGeneratorService.java     # Random number generation
│   │   │   └── infrastructure/                         # Infrastructure layer
│   │   │       ├── cli/                                # Command-line interface
│   │   │       │   ├── ConsoleView.java                # CLI implementation
│   │   │       │   └── GameController.java             # Application orchestrator
│   │   │       ├── persistence/                        # Data persistence
│   │   │       │   └── InMemoryGameRepository.java     # In-memory storage
│   │   │       └── factory/                            # Object creation
│   │   │           └── GameFactory.java                # Domain object factory
│   │   └── resources/
│   └── test/
│       └── java/
├── UML Diagrams/                                        # Architecture documentation
│   ├── ClassDiagram.md                                 # Class relationships
│   ├── ComponentDiagram.md                             # Component interactions
│   ├── PackageDiagram.md                               # Package structure
│   ├── SequenceDiagram.md                              # Sequence flows
│   └── UseCaseDiagram.md                               # Use case scenarios
├── pom.xml                                             # Maven project descriptor
├── README.md                                           # Project documentation
├── TO-DO.txt                                           # Implementation tracking
└── .gitignore                                          # Git ignore file
```

## Architecture

### Hexagonal Architecture (Ports and Adapters)
The application follows hexagonal architecture principles with clear separation of concerns:

#### **Domain Layer (Core)**
- **Pure Domain Entities**: Game, Player, GameState with minimal business logic
- **Rich Domain Services**: GameService handles complex business operations
- **Value Objects**: GameDifficulty with strategy pattern implementation
- **Domain Events**: GuessResult enum for clean state management

#### **Application Layer**
- **Ports**: Interfaces defining contracts for external dependencies
- **Use Cases**: Application business logic orchestrating domain operations
- **Dependency Injection**: Constructor-based dependency management

#### **Infrastructure Layer**
- **Adapters**: Concrete implementations of ports
- **CLI Interface**: ConsoleView for user interaction
- **Data Storage**: InMemoryGameRepository for persistence
- **Factories**: GameFactory for object creation

### Design Patterns Used

1. **Strategy Pattern**: Difficulty levels implementation
2. **Factory Pattern**: Object creation in GameFactory
3. **Repository Pattern**: Data access abstraction
4. **Use Case Pattern**: Application business logic organization
5. **Dependency Injection**: Constructor-based dependency management
6. **Anemic Domain Model**: Game entity focuses on data, GameService handles business logic

## Maven Configuration
The project includes Maven plugins for easy compilation, execution, and packaging:

### **Maven Exec Plugin**
Allows running the application directly with Maven:
```bash
mvn exec:java
```

### **Maven JAR Plugin**
Creates an executable JAR file:
```bash
mvn clean package
java -jar target/number-guessing-game-1.0.0.jar
```

### **Maven Compiler Plugin**
Ensures proper Java 23 compilation with UTF-8 encoding.

## Configuration
The application uses a simple configuration approach with dependency injection:

```java
// Main application setup
NumberGuessingCLI app = new NumberGuessingCLI();
GameController gameController = app.initializeDependencies();
gameController.startGame();
```

## Dependencies
- **JUnit 5.11.1**: Unit testing framework (test scope)
- **Java 23**: Modern Java features and syntax
- **Maven**: Build and dependency management

## Data Storage
- Games are stored in-memory using InMemoryGameRepository
- Data is automatically managed by the application
- No external database required - fully self-contained
- Thread-safe operations with synchronized blocks

## Key Features Implementation

### **Complete Game Flow**
- **Startup**: Welcome message and player setup
- **Gameplay**: Continuous guessing with real-time feedback
- **Completion**: Final results and play again option
- **Exit**: Graceful application termination

### **Error Handling**
- **Input Validation**: Comprehensive validation for all user inputs
- **State Validation**: Proper game state management
- **Exception Handling**: Graceful error recovery throughout
- **User Feedback**: Clear error messages and guidance

### **User Experience**
- **Professional UI**: Clean, formatted output with clear sections
- **Real-time Status**: Current game state and progress display
- **Helpful Hints**: Guidance for invalid input and game progression
- **Celebration Messages**: Success feedback and encouragement

### **Architecture Compliance**
- **Hexagonal Architecture**: Complete ports and adapters implementation
- **Dependency Inversion**: All dependencies follow SOLID principles
- **Single Responsibility**: Each class has one clear purpose
- **Open/Closed**: Easy to extend with new features

## Testing
The project includes comprehensive unit testing capabilities:

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=GameServiceTest

# Generate test coverage report
mvn jacoco:report
```

## Troubleshooting

### **Common Issues**

#### **"mvn command not found"**
- Install Maven following the instructions above
- Or use your IDE's built-in Maven support
- Or use Method 4 (direct compilation) above

#### **"Java version not supported"**
- Ensure you have Java 17 or higher installed
- Check with: `java -version`
- Update your Java installation if needed

#### **"Compilation errors"**
- Ensure all source files are present
- Check that you're in the correct directory
- Try cleaning and recompiling: `mvn clean compile`

#### **"Class not found" errors**
- Make sure you've compiled the project first
- Check that the classpath includes `target/classes`
- Verify the main class name is correct

### **IDE Setup**
- **IntelliJ IDEA**: Import as Maven project
- **Eclipse**: Import existing Maven project
- **VS Code**: Install Java Extension Pack and open the project folder

## Development Guidelines

### **Code Quality**
- Follow SOLID principles throughout
- Use comprehensive JavaDoc documentation
- Implement proper error handling
- Maintain thread safety where applicable

### **Architecture Principles**
- Keep domain layer pure and independent
- Use dependency injection for loose coupling
- Follow hexagonal architecture patterns
- Implement proper separation of concerns

### **Testing Strategy**
- Unit tests for all domain services
- Integration tests for use cases
- Mock external dependencies
- Test edge cases and error scenarios

---

**Enjoy playing the Number Guessing Game!** 🎮🎯
