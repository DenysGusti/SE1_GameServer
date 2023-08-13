package server.rules.player;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import messagesbase.UniqueGameIdentifier;
import messagesbase.messagesfromclient.PlayerRegistration;
import server.exceptions.RuleViolationException;
import server.game.Game;
import server.rules.IRule;

public class PlayerRegistrationCountRule implements IRule {
	private final Logger logger = LoggerFactory.getLogger(PlayerRegistrationCountRule.class);

	@Override
	public void handleRegisterPlayer(final Map<String, Game> games, final UniqueGameIdentifier gameID,
			final PlayerRegistration playerRegistration) throws RuleViolationException {
		final String gameIdentifier = gameID.getUniqueGameID();
		logger.info("Player registered to the following game " + gameIdentifier);
		if (games.get(gameIdentifier).getPlayerHandler().getPlayers().size() >= 2) {
			throw new RuleViolationException("PlayerRegistrationCountRule",
					"Already 2 Players are registrated!");
		}
	}
}
