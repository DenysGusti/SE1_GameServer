package server.rules.act;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import messagesbase.UniqueGameIdentifier;
import messagesbase.messagesfromclient.PlayerHalfMap;
import server.exceptions.BusinessRuleViolationException;
import server.game.Game;
import server.player.enumeration.EServerState;
import server.rules.IRule;

public class OnlyOnePlayerShouldActRule implements IRule {
	private final Logger logger = LoggerFactory.getLogger(OnlyOnePlayerShouldActRule.class);

	@Override
	public void handleHalfMap(final Map<String, Game> games, final UniqueGameIdentifier gameID, final PlayerHalfMap halfMap)
			throws BusinessRuleViolationException {
		logger.info("checking if its {} turn", halfMap.getUniquePlayerID());
		final String gameIdentifier = gameID.getUniqueGameID();
		
		games.get(gameIdentifier).getState().getStatePlayers().stream()
				.filter(player -> player.getPlayerID().equals(halfMap.getUniquePlayerID())
						&& player.getState() == EServerState.MUSTWAIT)
				.findFirst().ifPresent(player -> {
					throw new BusinessRuleViolationException(gameIdentifier, "OnlyOnePlayerShouldActRule",
							"It's not " + halfMap.getUniquePlayerID() + " turn!");
				});
	}
}
