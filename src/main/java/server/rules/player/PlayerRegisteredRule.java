package server.rules.player;

import java.util.Map;

import messagesbase.UniqueGameIdentifier;
import messagesbase.UniquePlayerIdentifier;
import messagesbase.messagesfromclient.PlayerHalfMap;
import server.exceptions.RuleViolationException;
import server.game.Game;
import server.rules.IRule;

public class PlayerRegisteredRule implements IRule {
	@Override
	public void handleHalfMap(final Map<String, Game> games, final UniqueGameIdentifier gameID, final PlayerHalfMap halfMap)
			throws RuleViolationException {
		final String playerID = halfMap.getUniquePlayerID();
		handleInCorrectPlayerID(games, gameID, playerID);
	}

	@Override
	public void handleGameState(final Map<String, Game> games, final UniqueGameIdentifier gameID, final UniquePlayerIdentifier playerID)
			throws RuleViolationException {
		final String playerIdentifier = playerID.getUniquePlayerID();
		handleInCorrectPlayerID(games, gameID, playerIdentifier);
	}

	private void handleInCorrectPlayerID(final Map<String, Game> games, final UniqueGameIdentifier gameID, String playerID)
			throws RuleViolationException {
		final String gameIdentifier = gameID.getUniqueGameID();
		if (!games.get(gameIdentifier).getPlayerHandler().getPlayers().values().stream().anyMatch(player -> player.getPlayerID().equals(playerID))) {
			throw new RuleViolationException("PlayerRegisteredRule",
					"{" + playerID + "} is not registered to this game: {" + gameID + "}");
		}
	}
}
