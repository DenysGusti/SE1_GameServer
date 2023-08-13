package server.rules.map;

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

public class NoIslandRuleTest {
	@Test
	public void handleHalfMap_MapHasNoIsland_DoesNotThrowException() {
		// arrange
		NoIslandRule islandRule = new NoIslandRule();
		String gameID = "game1";
		String playerID = UUID.randomUUID().toString();
		Map<String, Game> games = new HashMap<>();
		Collection<PlayerHalfMapNode> halfMapNode = new ArrayList<>();
		generateHalfMapWithoutIsland(halfMapNode);
		PlayerHalfMap halfMap = new PlayerHalfMap(playerID, halfMapNode);

		// act
		islandRule.handleHalfMap(games, new UniqueGameIdentifier(gameID), halfMap);

		// assert
		// No exception should be thrown
	}

	@Test
	public void handleHalfMap_MapHasIsland_ThrowsBusinessRuleViolationException() {
		// arrange
		NoIslandRule islandRule = new NoIslandRule();
		String gameID = "game2";
		String playerID = UUID.randomUUID().toString();
		Map<String, Game> games = new HashMap<>();
		Collection<PlayerHalfMapNode> halfMapNode = new ArrayList<>();
		generateHalfMapWithIsland(halfMapNode);
		PlayerHalfMap halfMap = new PlayerHalfMap(playerID, halfMapNode);

		// act & assert
		Executable target = () -> {
			islandRule.handleHalfMap(games, new UniqueGameIdentifier(gameID), halfMap);
		};
		Assertions.assertThrows(BusinessRuleViolationException.class, target);
	}

	private void generateHalfMapWithoutIsland(Collection<PlayerHalfMapNode> halfMapNode) {
		for (int yCoordinate = 0; yCoordinate < 5; yCoordinate++) {
			for (int xCoordinate = 0; xCoordinate < 10; xCoordinate++) {
				PlayerHalfMapNode node = new PlayerHalfMapNode(xCoordinate, yCoordinate, false, ETerrain.Grass);
				halfMapNode.add(node);
			}
		}
	}
	
	private void generateHalfMapWithIsland(Collection<PlayerHalfMapNode> halfMapNode) {
		for (int yCoordinate = 0; yCoordinate < 5; yCoordinate++) {
			for (int xCoordinate = 0; xCoordinate < 10; xCoordinate++) {
				PlayerHalfMapNode node = new PlayerHalfMapNode(xCoordinate, yCoordinate, false, ETerrain.Grass);
				halfMapNode.add(node);
			}
		}
		
		halfMapNode.removeIf(node -> node.getX() == 4 && node.getY() == 1
	            || node.getX() == 3 && node.getY() == 2
	            || node.getX() == 4 && node.getY() == 3
	            || node.getX() == 5 && node.getY() == 2);
		
		halfMapNode.add(new PlayerHalfMapNode(4, 1, false, ETerrain.Water));
		halfMapNode.add(new PlayerHalfMapNode(3, 2, false, ETerrain.Water));
		halfMapNode.add(new PlayerHalfMapNode(4, 3, false, ETerrain.Water));
		halfMapNode.add(new PlayerHalfMapNode(5, 2, false, ETerrain.Water));
	}
}
