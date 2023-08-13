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

public class MapTilesLimitRuleTest {
	@Test
	public void handleHalfMap_MapWithValidTilesCount_DoesNotThrowException() {
		// arrange
		MapTilesLimitRule tilesLimitRule = new MapTilesLimitRule();
		String gameID = "game1";
		String playerID = UUID.randomUUID().toString();
		Map<String, Game> games = new HashMap<>();
		Collection<PlayerHalfMapNode> halfMapNode = new ArrayList<>();
		generateHalfMapWithValidTilesCount(halfMapNode);
		PlayerHalfMap halfMap = new PlayerHalfMap(playerID, halfMapNode);

		// assert
		assertDoesNotThrow(() -> {
			// act
			tilesLimitRule.handleHalfMap(games, new UniqueGameIdentifier(gameID), halfMap);
		});
	}

	@Test
	public void handleHalfMap_MapWithInvalidTilesCount_ThrowsBusinessRuleViolationException() {
		// arrange
		MapTilesLimitRule tilesLimitRule = new MapTilesLimitRule();
		String gameID = "game1";
		String playerID = UUID.randomUUID().toString();
		Map<String, Game> games = new HashMap<>();
		Collection<PlayerHalfMapNode> halfMapNode = new ArrayList<>();
		generateHalfMapWithInvalidTilesCount(halfMapNode);
		PlayerHalfMap halfMap = new PlayerHalfMap(playerID, halfMapNode);

		/// act
		Executable target = () -> {
			tilesLimitRule.handleHalfMap(games, new UniqueGameIdentifier(gameID), halfMap);

		};
		// assert
		Assertions.assertThrows(BusinessRuleViolationException.class, target);
	}

	private void generateHalfMapWithValidTilesCount(Collection<PlayerHalfMapNode> halfMapNode) {
		for (int x = 0; x < 10; x++) {
			for (int y = 0; y < 5; y++) {
				halfMapNode.add(new PlayerHalfMapNode(x, y, false, ETerrain.Grass));
			}
		}
	}

	private void generateHalfMapWithInvalidTilesCount(Collection<PlayerHalfMapNode> halfMapNode) {
		for (int x = 0; x < 11; x++) {
			for (int y = 0; y < 6; y++) {
				halfMapNode.add(new PlayerHalfMapNode(x, y, false, ETerrain.Grass));
			}
		}
	}
}
