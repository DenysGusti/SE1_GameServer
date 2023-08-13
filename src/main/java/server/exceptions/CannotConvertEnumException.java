package server.exceptions;

public class CannotConvertEnumException extends GenericExampleException {

	private static final long serialVersionUID = 1L;

	public CannotConvertEnumException(final String errorMessage) {
        super("CannotConvertEnumException", errorMessage);
    }

}
