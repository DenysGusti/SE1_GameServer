package server.rules.map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import messagesbase.UniqueGameIdentifier;
import messagesbase.messagesfromclient.ETerrain;
import messagesbase.messagesfromclient.PlayerHalfMap;
import messagesbase.messagesfromclient.PlayerHalfMapNode;
import server.exceptions.BusinessRuleViolationException;
import server.game.Game;

public class AllCoordinatesCoveredRuleTest {
	@Test
	public void handleHalfMap_CreateMapWithValidCoordinates_DoesNotThrowException() {
		// arrange
		AllCoordinatesCoveredRule coordinatesRule = new AllCoordinatesCoveredRule();
		String gameID = "game1";
		String playerID = UUID.randomUUID().toString();
		Map<String, Game> games = new HashMap<>();
		Collection<PlayerHalfMapNode> halfMapNode = new ArrayList<>();
		generateHalfMapWithValidCoordinates(halfMapNode);
		PlayerHalfMap halfMap = new PlayerHalfMap(playerID, halfMapNode);

		// assert
		assertDoesNotThrow(() -> {
			// act
			coordinatesRule.handleHalfMap(games, new UniqueGameIdentifier(gameID), halfMap);
		});
	}

	@Test
	public void handleHalfMap_CreateMapWithInvalidCoordinates_ThrowsBusinessRuleViolationException() {
		// arrange
		AllCoordinatesCoveredRule coordinatesRule = new AllCoordinatesCoveredRule();
		String gameID = "game1";
		String playerID = UUID.randomUUID().toString();
		Map<String, Game> games = new HashMap<>();
		Collection<PlayerHalfMapNode> halfMapNode = new ArrayList<>();
		generateHalfMapWithInvalidCoordinates(halfMapNode);
		PlayerHalfMap halfMap = new PlayerHalfMap(playerID, halfMapNode);

		/// act
		Executable target = () -> {
			coordinatesRule.handleHalfMap(games, new UniqueGameIdentifier(gameID), halfMap);

		};
		// assert
		Assertions.assertThrows(BusinessRuleViolationException.class, target);
	}

	private void generateHalfMapWithValidCoordinates(Collection<PlayerHalfMapNode> halfMapNode) {
		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 5; j++) {
				halfMapNode.add(new PlayerHalfMapNode(i, j, false, ETerrain.Grass));
			}
		}
	}

	private void generateHalfMapWithInvalidCoordinates(Collection<PlayerHalfMapNode> halfMapNode) {

		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 5; j++) {
				halfMapNode.add(new PlayerHalfMapNode(i, j, false, ETerrain.Grass));
			}
		}
		// add a tile with invalid x coordinate
		halfMapNode.add(new PlayerHalfMapNode(10, 2, false, ETerrain.Grass));
		// add a tile with invalid y coordinate
		halfMapNode.add(new PlayerHalfMapNode(5, 5, false, ETerrain.Grass));
		// add a duplicate tile
		halfMapNode.add(new PlayerHalfMapNode(5, 2, false, ETerrain.Grass));
	}
}
