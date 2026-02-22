package server.exception;

public class TooSlowActionException extends GenericServerException {
    public TooSlowActionException() {
        super("You must do an action at least every 5 seconds.");
    }
}
