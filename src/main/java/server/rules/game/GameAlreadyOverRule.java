package server.rules.game;

import java.util.Map;

import messagesbase.UniqueGameIdentifier;
import messagesbase.messagesfromclient.PlayerHalfMap;
import server.exceptions.RuleViolationException;
import server.game.Game;
import server.player.enumeration.EServerState;
import server.rules.IRule;

public class GameAlreadyOverRule implements IRule {

	@Override
	public void handleHalfMap(final Map<String, Game> games, final UniqueGameIdentifier gameID,
			final PlayerHalfMap halfMap) throws RuleViolationException {
		final String gameIdentifier = gameID.getUniqueGameID();

		games.get(gameIdentifier).getState().getStatePlayers().stream().filter(
				player -> (player.getState().equals(EServerState.LOST) || player.getState().equals(EServerState.WON)))
				.findFirst().ifPresent(player -> {
					throw new RuleViolationException("GameAlreadyOverRule", "Game is already over");
				});
	}
}
