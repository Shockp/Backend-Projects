package com.shockp.numberguessinggame.infrastructure.cli;

import com.shockp.numberguessinggame.application.port.UserInterface;
import com.shockp.numberguessinggame.application.usecase.EndGameUseCase;
import com.shockp.numberguessinggame.application.usecase.MakeGuessUseCase;
import com.shockp.numberguessinggame.application.usecase.StartGameUseCase;
import com.shockp.numberguessinggame.domain.model.Game;

public class GameController {
    private final StartGameUseCase startGameUseCase;
    private final MakeGuessUseCase makeGuessUseCase;
    private final EndGameUseCase endGameUseCase;
    private final UserInterface userInterface;

    public GameController(StartGameUseCase startGameUseCase, MakeGuessUseCase makeGuessUseCase,
                          EndGameUseCase endGameUseCase, UserInterface userInterface) {
        if (startGameUseCase == null || makeGuessUseCase == null || 
            endGameUseCase == null || userInterface == null) {
            throw new IllegalArgumentException("All parameters must be non-null");
        }

        this.startGameUseCase = startGameUseCase;
        this.makeGuessUseCase = makeGuessUseCase;
        this.endGameUseCase = endGameUseCase;
        this.userInterface = userInterface;
    }

    public void startGame() {
        
    }

    public void runGameLoop() {

    }

    public void processUserInput(String input) {

    }

    public void handleGameState(Game game) {

    }

    public void displayWelcomeMessage() {

    }

    public void displayGoodbyeMessage() {

    }
}
