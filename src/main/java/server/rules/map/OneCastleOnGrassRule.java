package server.rules.map;

import java.util.Collection;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import messagesbase.UniqueGameIdentifier;
import messagesbase.messagesfromclient.ETerrain;
import messagesbase.messagesfromclient.PlayerHalfMap;
import messagesbase.messagesfromclient.PlayerHalfMapNode;
import server.exceptions.BusinessRuleViolationException;
import server.exceptions.RuleViolationException;
import server.game.Game;
import server.rules.IRule;

public class OneCastleOnGrassRule implements IRule {
	private final Logger logger = LoggerFactory.getLogger(OneCastleOnGrassRule.class);
	
	@Override
	public void handleHalfMap(final Map<String, Game> games, final UniqueGameIdentifier gameID, final PlayerHalfMap halfMap)
			throws RuleViolationException {
		logger.trace("Check if Castle is placed...");
		final Collection<PlayerHalfMapNode> node = halfMap.getMapNodes();
		if (!handleCastleAmount(node) || handleMisplacedCastle(node)) {
			throw new BusinessRuleViolationException(gameID.getUniqueGameID(), "OneCastleOnGrassRule",
					"Insufficient placement of castle");
		}
	}

	private boolean handleCastleAmount(final Collection<PlayerHalfMapNode> node) {
		final long moreThanOneCastle = node.stream().filter(tile -> tile.isFortPresent()).count();
		logger.debug("Castle amount: " + moreThanOneCastle);
		return moreThanOneCastle == 1;
	}

	private boolean handleMisplacedCastle(final Collection<PlayerHalfMapNode> node) {
		return node.stream().anyMatch(tile -> tile.isFortPresent() && tile.getTerrain() != ETerrain.Grass);
	}
}
