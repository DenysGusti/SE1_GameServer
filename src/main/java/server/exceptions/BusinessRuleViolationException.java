package server.exceptions;

import server.game.GameManager;

public class BusinessRuleViolationException extends RuleViolationException {

	private static final long serialVersionUID = 1L;
	private final String gameID;

	public BusinessRuleViolationException(final String gameID, final String errorString, final String errorMessage) {
		super(errorString, errorMessage);
		this.gameID = gameID;
	}

	@Override
	public void processGameState(final GameManager gameManager) {
		gameManager.getGames().get(gameID).getState().setGameOutcome();
	}
}
