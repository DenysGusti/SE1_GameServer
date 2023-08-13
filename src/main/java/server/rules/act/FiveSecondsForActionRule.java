package server.rules.act;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

import messagesbase.UniqueGameIdentifier;
import messagesbase.messagesfromclient.PlayerHalfMap;
import server.exceptions.BusinessRuleViolationException;
import server.game.Game;
import server.player.enumeration.EServerState;
import server.rules.IRule;

public class FiveSecondsForActionRule implements IRule {
	
	@Override
	public void handleHalfMap(final Map<String, Game> games, final UniqueGameIdentifier gameID, final PlayerHalfMap halfMap)
			throws BusinessRuleViolationException {
		handleFiveSecondsTimer(games, gameID, halfMap.getUniquePlayerID());
	}

	private void handleFiveSecondsTimer(final Map<String, Game> games, final UniqueGameIdentifier gameID, final String playerID)
			throws BusinessRuleViolationException {
		final Duration TURN_TIME_LIMIT = Duration.ofSeconds(5);
		final LocalDateTime now = LocalDateTime.now();
		final String gameIdentifier = gameID.getUniqueGameID();
		games.get(gameIdentifier).getState().getStatePlayers().stream().filter(player -> player.getPlayerID().equals(playerID))
				.findFirst().ifPresent(player -> {
					if (player.getState().equals(EServerState.MUSTACT)) {
						if (player.getLastActionTime().plus(TURN_TIME_LIMIT).isBefore(now)) {
							throw new BusinessRuleViolationException(gameIdentifier,
									playerID + " took longer than 5 seconds to act", "FiveSecondsForActionRule");
						}
					}
				});
	}
}
