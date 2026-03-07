package server.exception;

public class GameRoundException extends GenericServerException {
    public GameRoundException() {
        super("Game has too many rounds, try more efficient strategies.");
    }
}
