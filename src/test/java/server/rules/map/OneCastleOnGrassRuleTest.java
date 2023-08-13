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

public class OneCastleOnGrassRuleTest {
	@Test
	public void handleHalfMap_ValidCastlePlacementOnMap_DoesNotThrowException() {
		// arrange
		OneCastleOnGrassRule castleRule = new OneCastleOnGrassRule();
		String gameID = "game1";
		String playerID = UUID.randomUUID().toString();
		Map<String, Game> games = new HashMap<>();
		Collection<PlayerHalfMapNode> halfMapNode = new ArrayList<>();

		generateHalfMapWithOnlyGrassTiles(halfMapNode);
		PlayerHalfMapNode castleNode = new PlayerHalfMapNode(3, 2, true, ETerrain.Grass);
		halfMapNode.add(castleNode);

		PlayerHalfMap halfMap = new PlayerHalfMap(playerID, halfMapNode);

		// assert
		assertDoesNotThrow(() -> {
			// act
			castleRule.handleHalfMap(games, new UniqueGameIdentifier(gameID), halfMap);
		});
	}

	@Test
	public void handleHalfMap_InvalidCastleAmountOfTwo_ThrowsBusinessRuleViolationException() {
		// arrange
		OneCastleOnGrassRule castleRule = new OneCastleOnGrassRule();
		String gameID = "game1";
		String playerID = UUID.randomUUID().toString();
		Map<String, Game> games = new HashMap<>();
		Collection<PlayerHalfMapNode> halfMapNode = new ArrayList<>();

		generateHalfMapWithOnlyGrassTiles(halfMapNode);
		PlayerHalfMapNode castleNode1 = new PlayerHalfMapNode(3, 2, true, ETerrain.Grass);
		PlayerHalfMapNode castleNode2 = new PlayerHalfMapNode(4, 3, true, ETerrain.Grass);
		halfMapNode.add(castleNode1);
		halfMapNode.add(castleNode2);
		PlayerHalfMap halfMap = new PlayerHalfMap(playerID, halfMapNode);

		// act
		Executable target = () -> {
			castleRule.handleHalfMap(games, new UniqueGameIdentifier(gameID), halfMap);
		};

		// assert
		Assertions.assertThrows(BusinessRuleViolationException.class, target);
	}

	@Test
	public void handleHalfMap_InvalidCastlePlacementOnMap_ThrowsBusinessRuleViolationException() {
		// arrange
		OneCastleOnGrassRule castleRule = new OneCastleOnGrassRule();
		String gameID = "game1";
		String playerID = UUID.randomUUID().toString();
		Map<String, Game> games = new HashMap<>();
		Collection<PlayerHalfMapNode> halfMapNode = new ArrayList<>();

		generateHalfMapWithOnlyGrassTiles(halfMapNode);
		PlayerHalfMapNode castleNode = new PlayerHalfMapNode(3, 2, true, ETerrain.Mountain);
		halfMapNode.add(castleNode);
		PlayerHalfMap halfMap = new PlayerHalfMap(playerID, halfMapNode);

		// act
		Executable target = () -> {
			castleRule.handleHalfMap(games, new UniqueGameIdentifier(gameID), halfMap);
		};
		// assert
		Assertions.assertThrows(BusinessRuleViolationException.class, target);
	}

	private void generateHalfMapWithOnlyGrassTiles(Collection<PlayerHalfMapNode> halfMapNode) {
		for (int yCoordinate = 0; yCoordinate < 5; yCoordinate++) {
			for (int xCoordinate = 0; xCoordinate < 10; xCoordinate++) {
				PlayerHalfMapNode node = new PlayerHalfMapNode(xCoordinate, yCoordinate, false, ETerrain.Grass);
				halfMapNode.add(node);
			}
		}
	}
}
