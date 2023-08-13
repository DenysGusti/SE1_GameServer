package server.rules.gameid;

import java.util.Map;

import messagesbase.UniqueGameIdentifier;
import messagesbase.UniquePlayerIdentifier;
import messagesbase.messagesfromclient.PlayerHalfMap;
import messagesbase.messagesfromclient.PlayerRegistration;
import server.exceptions.RuleViolationException;
import server.game.Game;
import server.rules.IRule;

public class ExistingGameIDRule implements IRule {

	@Override
	public void handleGameID(final Map<String, Game> games, final UniqueGameIdentifier gameID) throws RuleViolationException {
		final String gameIdentifier = gameID.getUniqueGameID();
		if (games.containsKey(gameIdentifier)) {
			return;
		}
		throw new RuleViolationException("RuleViolationException", "GameID does not exist!");
	}

	@Override
	public void handleRegisterPlayer(final Map<String, Game> games, final UniqueGameIdentifier gameID,
			final PlayerRegistration playerRegistration) throws RuleViolationException {
		handleGameID(games, gameID);
	}

	@Override
	public void handleHalfMap(final Map<String, Game> games, final UniqueGameIdentifier gameID, final PlayerHalfMap halfMap) {
		handleGameID(games, gameID);
	}

	@Override
	public void handleGameState(final Map<String, Game> games, final UniqueGameIdentifier gameID, final UniquePlayerIdentifier playerID)
			throws RuleViolationException {
		handleGameID(games, gameID);
	}
}
