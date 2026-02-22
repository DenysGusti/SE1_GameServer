package server.exception;

public class PlayerRegisterRuleException extends GenericServerException {
    public PlayerRegisterRuleException() {
        super("More than the expected number of clients has tried to register. At most 2 clients can take part in a " +
                "single game. As of now 2 are already registered. Hence, the received additional client registration " +
                "is not permitted. Note, if you have created a game with a dummy enemy you need to register only a " +
                "single client (i.e., your client once) to start playing. The second client will be provided by the " +
                "server (i.e., the dummy enemy).");
    }
}
