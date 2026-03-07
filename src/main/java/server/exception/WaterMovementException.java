package server.exception;

import messagesbase.messagesfromclient.EMove;
import server.data.XYPair;

public class WaterMovementException extends GenericServerException {
    public WaterMovementException(XYPair playerPosition, XYPair newPosition, EMove move) {
        super("Player attempted to move into water: " + playerPosition + " -> " + newPosition + " with move: " + move);
    }
}
