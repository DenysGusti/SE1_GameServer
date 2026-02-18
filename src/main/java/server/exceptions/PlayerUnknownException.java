package server.exceptions;

public class PlayerUnknownException extends GenericServerException {
    public PlayerUnknownException(String gameId, String playerId) {
        super("Game state information was requested for a player ID which is unknown for the game: GameIdentifier=" +
                gameId + " Player ID the request was sent for: PlayerIdentifier=" + playerId + " Check for typical " +
                "errors: a) player ID mixed up with game ID; or b) your code does not use the Player ID types' " +
                "getters to access its real raw value (see, the documentation); or c) mixing in an implicit/explicit " +
                "call on a Player ID types' toString().");
    }
}