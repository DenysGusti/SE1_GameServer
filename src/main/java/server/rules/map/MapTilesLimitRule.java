package server.rules.map;

import java.util.Collection;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import messagesbase.UniqueGameIdentifier;
import messagesbase.messagesfromclient.PlayerHalfMap;
import messagesbase.messagesfromclient.PlayerHalfMapNode;
import server.exceptions.BusinessRuleViolationException;
import server.exceptions.RuleViolationException;
import server.game.Game;
import server.rules.IRule;

public class MapTilesLimitRule implements IRule {
	// to avoid using magic numbers -> replace with named constant
	private final static int TILES_COUNT_LIMIT = 50;
	private final Logger logger = LoggerFactory.getLogger(MapTilesLimitRule.class);
	
	@Override
	public void handleHalfMap(final Map<String, Game> games, final UniqueGameIdentifier gameID, final PlayerHalfMap halfMap)
			throws RuleViolationException {
		final Collection<PlayerHalfMapNode> node = halfMap.getMapNodes();
		logger.trace("Check tiles amount...");
		if (node.size() != TILES_COUNT_LIMIT) {
			throw new BusinessRuleViolationException(gameID.getUniqueGameID(), "MapTilesLimitRule", "Insufficient tiles count");
		}
	}
}
