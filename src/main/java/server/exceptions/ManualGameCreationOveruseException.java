package server.exceptions;

public class ManualGameCreationOveruseException extends GenericServerException {
    public ManualGameCreationOveruseException() {
        super("To maintain system performance and ensure fair access for all students, there is a limit on the " +
                "number of manually created games each student can run within a given time frame. Exceeding this " +
                "limit can degrade system performance and negatively affect your peers. It seems you have exceeded " +
                "the allowed limit, so this game has been stopped. The limit will reset automatically if no new " +
                "games are created for a few hours.");
    }
}
