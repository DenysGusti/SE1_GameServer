package server.exception;

import java.util.Objects;

/* Note; this is just a quick minimal example to show the generic exception handling
 * functionality in spring. Typically, I would recommend that you create individual exception types
 * for each unique kind of exception (e.g., individual exceptions for different kinds of rule errors)
 *
 * While doing so, follow the tips given in the Error Handling Tutorial.
 *
 * For simplicity reasons, your own exceptions could, e.g., inherit from the one given below
 */
public abstract class GenericServerException extends RuntimeException {
    public GenericServerException(String message) {
        super(Objects.requireNonNull(message, "message is null"));
    }
}
