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

public class MapTerrainsLimitRule implements IRule {
	// to avoid using magic numbers -> replace with named constant
	public static final int MIN_GRASS_TILES = 24;
	public static final int MIN_MOUNTAIN_TILES = 5;
	public static final int MIN_WATER_TILES = 7;
	private final Logger logger = LoggerFactory.getLogger(MapTerrainsLimitRule.class);

	@Override
	public void handleHalfMap(final Map<String, Game> games, final UniqueGameIdentifier gameID, final PlayerHalfMap halfMap)
			throws RuleViolationException {
		final Collection<PlayerHalfMapNode> node = halfMap.getMapNodes();
		if (!handleNumberOfTerrains(node)) {
			throw new BusinessRuleViolationException(gameID.getUniqueGameID(), "MapTerrainsLimitRule",
					"Insufficient terrain type amount on map");
		}
	}

	private boolean handleNumberOfTerrains(final Collection<PlayerHalfMapNode> node) {
		logger.trace("Check number of Terrains...");
		return node.stream().filter(tile -> tile.getTerrain().equals(ETerrain.Grass)).count() >= MIN_GRASS_TILES
				&& node.stream().filter(tile -> tile.getTerrain().equals(ETerrain.Mountain))
						.count() >= MIN_MOUNTAIN_TILES
				&& node.stream().filter(tile -> tile.getTerrain().equals(ETerrain.Water)).count() >= MIN_WATER_TILES;
	}
}
