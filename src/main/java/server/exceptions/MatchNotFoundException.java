package server.exceptions;

public class MatchNotFoundException extends GenericServerException {
    public MatchNotFoundException(String gameId) {
        super("Match was not found, i.e., a game with the given game ID did not exist, checked for game ID: " + gameId +
                " Check for typical errors: a) the provided game ID wasn't correct; " +
                "or b) the game and player ID were mixed; or c) the game became to old and, thus, was deleted.");
    }
}
