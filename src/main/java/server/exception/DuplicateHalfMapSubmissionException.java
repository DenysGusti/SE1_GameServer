package server.exception;

public class DuplicateHalfMapSubmissionException extends GenericServerException {
    public DuplicateHalfMapSubmissionException() {
        super("You have already submitted a half map.");
    }
}
