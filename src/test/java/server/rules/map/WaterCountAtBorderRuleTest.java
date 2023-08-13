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

public class WaterCountAtBorderRuleTest {
	@Test
	public void handleHalfMap_ValidWaterFieldsAtBorder_DoesNotThrowException() {
		// arrange
		WaterCountAtBorderRule waterCountRule = new WaterCountAtBorderRule();
		String gameID = "game1";
		String playerID = UUID.randomUUID().toString();
		Map<String, Game> games = new HashMap<>();
		Collection<PlayerHalfMapNode> halfMapNode = new ArrayList<>();
		generateHalfMapWithValidWaterFieldsAtBorder(halfMapNode);
		PlayerHalfMap halfMap = new PlayerHalfMap(playerID, halfMapNode);

		// assert
		assertDoesNotThrow(() -> {
			// act
			waterCountRule.handleHalfMap(games, new UniqueGameIdentifier(gameID), halfMap);
		});
	}

	@Test
	public void handleHalfMap_TooManyWaterFieldsAtBorder_ThrowsBusinessRuleViolationException() {
		// arrange
		WaterCountAtBorderRule waterCountRule = new WaterCountAtBorderRule();
		String gameID = "game1";
		String playerID = UUID.randomUUID().toString();
		Map<String, Game> games = new HashMap<>();
		Collection<PlayerHalfMapNode> halfMapNode = new ArrayList<>();
		generateHalfMapWithTooManyWaterFieldsAtBorder(halfMapNode);
		PlayerHalfMap halfMap = new PlayerHalfMap(playerID, halfMapNode);

		// act
		Executable target = () -> {
			waterCountRule.handleHalfMap(games, new UniqueGameIdentifier(gameID), halfMap);

		};
		// assert
		Assertions.assertThrows(BusinessRuleViolationException.class, target);
	}

	private void generateHalfMapWithValidWaterFieldsAtBorder(Collection<PlayerHalfMapNode> halfMapNode) {
		for (int x = 0; x < 10; x++) {
			for (int y = 0; y < 5; y++) {
				halfMapNode.add(new PlayerHalfMapNode(x, y, false, ETerrain.Grass));
			}
		}
		halfMapNode.removeIf(node -> node.getX() == 0 && node.getY() == 0
	            || node.getX() == 0 && node.getY() == 1
	            || node.getX() == 1 && node.getY() == 1
	            || node.getX() == 1 && node.getY() == 2
	            || node.getX() == 1 && node.getY() == 3
	            || node.getX() == 1 && node.getY() == 4);
		
		halfMapNode.add(new PlayerHalfMapNode(0, 0, false, ETerrain.Water));
		halfMapNode.add(new PlayerHalfMapNode(0, 1, false, ETerrain.Water));
		
		halfMapNode.add(new PlayerHalfMapNode(1, 1, false, ETerrain.Water));
		halfMapNode.add(new PlayerHalfMapNode(1, 2, false, ETerrain.Water));
		halfMapNode.add(new PlayerHalfMapNode(1, 3, false, ETerrain.Water));
		halfMapNode.add(new PlayerHalfMapNode(1, 4, false, ETerrain.Water));
	}

	private void generateHalfMapWithTooManyWaterFieldsAtBorder(Collection<PlayerHalfMapNode> halfMapNode) {
		for (int x = 0; x < 10; x++) {
			for (int y = 0; y < 5; y++) {
				ETerrain terrain = ETerrain.Grass;
				if ((x == 0 || x == 9) && y <= 3) {
					terrain = ETerrain.Water;
				}
				if ((y == 0 || y == 4) && x <= 5) {
					terrain = ETerrain.Water;
				}
				halfMapNode.add(new PlayerHalfMapNode(x, y, false, terrain));
			}
		}
	}
}
