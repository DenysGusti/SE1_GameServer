package server.exceptions;

public class UnknownUAccountException extends GenericServerException {
    public UnknownUAccountException(String uAccount) {
        super("Use your real UniVie u:account username during the game registration process. " +
                "Otherwise progress related bonus points, AI evaluations etc. cannot be attributed. " +
                "You used following identifier: " + uAccount);
    }
}
