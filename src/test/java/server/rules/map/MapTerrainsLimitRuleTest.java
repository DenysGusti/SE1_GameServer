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

public class MapTerrainsLimitRuleTest {
	@Test
	public void handleHalfMap_CreateMapWithValidTerrainCount_DoesNotThrowException() {
		// arrange
		MapTerrainsLimitRule terrainRule = new MapTerrainsLimitRule();
		String gameID = "game1";
		String playerID = UUID.randomUUID().toString();
		
		Map<String, Game> games = new HashMap<>();
		Collection<PlayerHalfMapNode> halfMapNode = new ArrayList<>();
		generateHalfMapWithValidTerrainsCount(halfMapNode);
		PlayerHalfMap halfMap = new PlayerHalfMap(playerID, halfMapNode);

		// assert
		assertDoesNotThrow(() -> {
			// act
			terrainRule.handleHalfMap(games, new UniqueGameIdentifier(gameID), halfMap);
		});
	}

	@Test
	public void handleHalfMap_CreateMapWithInvalidTerrainCount_ThrowsBusinessRuleViolationException() {
		// arrange
		MapTerrainsLimitRule terrainRule = new MapTerrainsLimitRule();
		String gameID = "game1";
		String playerID = UUID.randomUUID().toString();
		Map<String, Game> games = new HashMap<>();
		Collection<PlayerHalfMapNode> halfMapNode = new ArrayList<>();
		generateHalfMapWithInvalidTerrainsCount(halfMapNode);
		PlayerHalfMap halfMap = new PlayerHalfMap(playerID, halfMapNode);

		/// act
		Executable target = () -> {
			terrainRule.handleHalfMap(games, new UniqueGameIdentifier(gameID), halfMap);

		};
		// assert
		Assertions.assertThrows(BusinessRuleViolationException.class, target);
	}

	private void generateHalfMapWithValidTerrainsCount(Collection<PlayerHalfMapNode> halfMapNode) {
		int grassCount = 0;
		int mountainCount = 0;
		int waterCount = 0;
		for (int x = 0; x < 10; x++) {
			for (int y = 0; y < 5; y++) {
				ETerrain terrain = ETerrain.Grass;
				if (grassCount < 25) {
					grassCount++;
				} else if (mountainCount < 5) {
					terrain = ETerrain.Mountain;
					mountainCount++;
				} else if (waterCount < 7) {
					terrain = ETerrain.Water;
					waterCount++;
				}
				halfMapNode.add(new PlayerHalfMapNode(x, y, false, terrain));
			}
		}
	}

	private void generateHalfMapWithInvalidTerrainsCount(Collection<PlayerHalfMapNode> halfMapNode) {
		for (int x = 0; x < 20; x++) {
			for (int y = 0; y < 5; y++) {
				halfMapNode.add(new PlayerHalfMapNode(x, y, false, ETerrain.Grass));
			}
		}
		for (int x = 0; x < 3; x++) {
			for (int y = 0; y < 2; y++) {
				halfMapNode.add(new PlayerHalfMapNode(x, y, false, ETerrain.Mountain));
			}
		}
		for (int x = 0; x < 5; x++) {
			for (int y = 0; y < 1; y++) {
				halfMapNode.add(new PlayerHalfMapNode(x, y, false, ETerrain.Water));
			}
		}
	}
}
