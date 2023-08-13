package server.rules.map;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

public class WaterCountAtBorderRule implements IRule {
	// to avoid using magic numbers -> replace with named constant
	private final static int X_AXIS_WATERFIELDS_LIMIT = 4;
	private final static int Y_AXIS_WATERFIELDS_LIMIT = 2;
	private final static int X_AXIS_RIGHT = 9;
	private final static int X_AXIS_LEFT = 0;
	private final static int Y_AXIS_UP = 4;
	private final static int Y_AXIS_DOWN = 0;

	private final Logger logger = LoggerFactory.getLogger(WaterCountAtBorderRule.class);
	
	@Override
	public void handleHalfMap(final Map<String, Game> games, final UniqueGameIdentifier gameID, final PlayerHalfMap halfMap)
			throws RuleViolationException {
		final Collection<PlayerHalfMapNode> node = halfMap.getMapNodes();
		if (!handleWaterCountAtBorder(node)) {
			throw new BusinessRuleViolationException(gameID.getUniqueGameID(), "WaterCountAtBorderRule",
					"Insufficient water count on borders");
		}
	}

	private boolean handleWaterCountAtBorder(final Collection<PlayerHalfMapNode> node) {
		logger.trace("Check water count at border...");
		List<PlayerHalfMapNode> waterBorderMap = node.stream().filter(tile -> tile.getTerrain() == ETerrain.Water)
				.collect(Collectors.toList());
		return waterBorderMap.stream().filter(tile -> tile.getX() == X_AXIS_LEFT).count() <= Y_AXIS_WATERFIELDS_LIMIT
				&& waterBorderMap.stream().filter(tile -> tile.getX() == X_AXIS_RIGHT)
						.count() <= Y_AXIS_WATERFIELDS_LIMIT
				&& waterBorderMap.stream().filter(tile -> tile.getY() == Y_AXIS_DOWN)
						.count() <= X_AXIS_WATERFIELDS_LIMIT
				&& waterBorderMap.stream().filter(tile -> tile.getY() == Y_AXIS_UP).count() <= X_AXIS_WATERFIELDS_LIMIT;
	}
}
