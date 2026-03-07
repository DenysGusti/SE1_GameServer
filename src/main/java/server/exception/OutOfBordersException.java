package server.exception;

import messagesbase.messagesfromclient.EMove;
import server.data.XYPair;

public class OutOfBordersException extends GenericServerException {
    public OutOfBordersException(XYPair playerPosition, XYPair newPosition, EMove move) {
        super("Player attempted to move outside of board bounds: " + playerPosition + " -> " + newPosition + " with move: " + move);
    }
}
