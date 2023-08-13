package server.map.support;

import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import server.game.ServerGameState;
import server.map.Coordinate;
import server.map.ServerMap;
import server.map.ServerTile;
import server.player.Player;

public class MapEditor {
	private static final Logger logger = LoggerFactory.getLogger(MapEditor.class);

	public static ServerMap editMapForPlayer(final ServerGameState state, final String playerID) {
		final ServerGameState deepCopyState = (ServerGameState) state.clone();
		final ServerMap deepCopyMap = deepCopyState.getMap();

		final List<Player> players = deepCopyState.getStatePlayers();
		final int index = players
				.indexOf(players.stream().filter(player -> player.getPlayerID().equals(playerID)).findFirst().get());

		PlayerStateManager.initializePlayerState(deepCopyMap, index);
		TreasureManager.hideTreasure(deepCopyMap);

		Optional<Entry<Coordinate, ServerTile>> playerNode = deepCopyMap.getMap().entrySet().stream()
				.filter(tile -> tile.getValue().isFortPresent()).findFirst();

		playerNode.ifPresent(node -> {
			logger.info("FMN [P={}, T={}, TS={}, FS={}, X={}, Y={}]", node.getValue().getPositionState().name(),
					node.getValue().getTerrain().name(), node.getValue().getTreasureState().name(),
					node.getValue().getFortState().name(), node.getKey().getX(), node.getKey().getY());
		});

		Optional<Entry<Coordinate, ServerTile>> enemyNode = deepCopyMap.getMap().entrySet().stream()
				.filter(tile -> (tile.getValue().isEnemyPresent())).findFirst();

		enemyNode.ifPresent(node -> {
			logger.info("FMN [P={}, T={}, TS={}, FS={}, X={}, Y={}]", node.getValue().getPositionState().name(),
					node.getValue().getTerrain().name(), node.getValue().getTreasureState().name(),
					node.getValue().getFortState().name(), node.getKey().getX(), node.getKey().getY());
		});

		return deepCopyState.getMap();
	}
}