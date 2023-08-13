package server.exceptions;

public class RuleViolationException extends GenericExampleException {

	private static final long serialVersionUID = 1L;

	public RuleViolationException(final String errorString, final String errorMessage) {
		super(errorString, errorMessage);
	}
}
