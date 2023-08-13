package server.rules.map;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
import server.map.Coordinate;
import server.rules.IRule;

public class NoIslandRule implements IRule {
	/*
	 * TAKEN FROM <1> Implementation of flood fill algorithm using different
	 * languages and methods like recursive and BFS Source:
	 * https://www.geeksforgeeks.org/flood-fill-algorithm-implement-fill-paint“
	 */
	private final Logger logger = LoggerFactory.getLogger(NoIslandRule.class);

	@Override
	public void handleHalfMap(final Map<String, Game> games, final UniqueGameIdentifier gameID, final PlayerHalfMap halfMap)
			throws RuleViolationException {
		if (!checkIfThereIsNotAnIsland(halfMap.getMapNodes())) {
			throw new BusinessRuleViolationException(gameID.getUniqueGameID(), "NoIslandRule", "Map has islands");
		}
	}

	private boolean checkIfThereIsNotAnIsland(Collection<PlayerHalfMapNode> halfMapNode) {
		logger.trace("Check if there is an island...");
		final Set<Coordinate> visitedPlaces = new HashSet<>();
		final Map<Coordinate, ETerrain> map = convertCollectionToMap(halfMapNode);
		int counter = 0;
		for (final Coordinate coordinate : map.keySet()) {
			if (visitedPlaces.contains(coordinate)) {
				continue;
			}
			if (map.get(coordinate).equals(ETerrain.Grass) || map.get(coordinate).equals(ETerrain.Mountain)) {
				isAllAccessible(coordinate.getX(), coordinate.getY(), map, visitedPlaces);
				counter++;
			}
		}
		return counter == 1;

	}

	private Map<Coordinate, ETerrain> convertCollectionToMap(Collection<PlayerHalfMapNode> halfMapNode) {
		Map<Coordinate, ETerrain> map = halfMapNode.stream().collect(
				Collectors.toMap(node -> new Coordinate(node.getX(), node.getY()), node -> node.getTerrain()));
		return map;
	}

	private void isAllAccessible(final int x, final int y, final Map<Coordinate, ETerrain> map, final Set<Coordinate> visitedPlaces) {
		/*
		 * changes to the source code were needed for implementation but the main
		 * algorithm was taken from source TAKEN FROM START <1>
		 */
		if (x < 0 || y < 0 || x > 9 || y > 4) {
			return;
		}
		final Coordinate coordinate = new Coordinate(x, y);
		if (visitedPlaces.contains(coordinate)) {
			return;
		}
		if (map.get(coordinate).equals(ETerrain.Water)) {
			return;
		}
		visitedPlaces.add(coordinate);
		isAllAccessible(x + 1, y, map, visitedPlaces);
		isAllAccessible(x - 1, y, map, visitedPlaces);
		isAllAccessible(x, y + 1, map, visitedPlaces);
		isAllAccessible(x, y - 1, map, visitedPlaces);
		/*
		 * TAKEN FROM END <1>
		 */
	}
}
