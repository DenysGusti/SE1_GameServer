package server.rules.map;

import java.util.Collection;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import messagesbase.UniqueGameIdentifier;
import messagesbase.messagesfromclient.PlayerHalfMap;
import messagesbase.messagesfromclient.PlayerHalfMapNode;
import server.exceptions.BusinessRuleViolationException;
import server.game.Game;
import server.map.Coordinate;
import server.rules.IRule;

public class AllCoordinatesCoveredRule implements IRule {
	private final int MIN_HALF_MAP_X = 0;
	private final int MAX_HALF_MAP_X = 9;
	private final int MIN_HALF_MAP_Y = 0;
	private final int MAX_HALF_MAP_Y = 4;
	private final Logger logger = LoggerFactory.getLogger(AllCoordinatesCoveredRule.class);

	@Override
	public void handleHalfMap(final Map<String, Game> games, final UniqueGameIdentifier gameID, final PlayerHalfMap halfMap)
			throws BusinessRuleViolationException {
		logger.trace("Check if map has correct coordinates..");
		if (!allXCoordinatesValid(halfMap.getMapNodes()) || !allYCoordinatesValid(halfMap.getMapNodes())
				|| !allUniqueCoordinates(halfMap.getMapNodes())) {
			throw new BusinessRuleViolationException(gameID.getUniqueGameID(), "AllCoordinatesCoveredRule",
					"Map has invalid or duplicates coordiantes");
		}
	}
	
	private boolean allXCoordinatesValid(final Collection<PlayerHalfMapNode> node) {
		return node.stream()
				.allMatch(coordinate -> coordinate.getX() >= MIN_HALF_MAP_X && coordinate.getX() <= MAX_HALF_MAP_X);
	}
	
	private boolean allYCoordinatesValid(final Collection<PlayerHalfMapNode> node) {
		return node.stream()
				.allMatch(coordinate -> coordinate.getY() >= MIN_HALF_MAP_Y && coordinate.getY() <= MAX_HALF_MAP_Y);
	}
	
	private boolean allUniqueCoordinates(final Collection<PlayerHalfMapNode> node) {
		return node.stream().map(coordinates -> new Coordinate(coordinates.getX(), coordinates.getY())).distinct()
				.count() == node.size();
	}

}
