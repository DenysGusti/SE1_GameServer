package server.exception;

public class PlayerTurnException extends GenericServerException {
    public PlayerTurnException() {
        super("It is not your turn to act.");
    }
}
