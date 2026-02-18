package server.exceptions;

public class TooFastPollingException extends GenericServerException {
    public TooFastPollingException() {
        super("A client requested the game state too quickly. You must wait at least 400 milliseconds (0.4 seconds) " +
                "between consecutive game state requests.");
    }
}
