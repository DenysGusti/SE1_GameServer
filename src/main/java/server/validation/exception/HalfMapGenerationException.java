package server.validation.exception;

import server.exception.GenericServerException;

import java.util.Objects;

public class HalfMapGenerationException extends GenericServerException {
    public HalfMapGenerationException(String message) {
        super(Objects.requireNonNull(message, "message is null"));
    }
}