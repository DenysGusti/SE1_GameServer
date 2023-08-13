package server.map;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import server.game.ServerGameState;
import server.map.enumeration.EMapSize;
import server.map.enumeration.EServerFortState;
import server.map.support.TreasureManager;
import server.player.Player;

public class MapHandler {
	private final Logger logger = LoggerFactory.getLogger(MapHandler.class);
	private final Map<String, ServerMap> playerMaps;
	private final Random random = new Random();
	private final EMapSize mapSize;
	private final static int MAXIMAL_MAP_REGISTER_SIZE = 2;

	public MapHandler() {
		this.playerMaps = new HashMap<String, ServerMap>();
		this.mapSize = chooseMapSize();
	}

	public Map<String, ServerMap> getPlayerMaps() {
		return playerMaps;
	}

	public void addPlayerMaps(final String playerID, final ServerMap serverMap, final ServerGameState state) {
		logger.info("Map from player with playerID: {} saved", playerID);
		playerMaps.put(playerID, serverMap);
		TreasureManager.setTreasureState(serverMap);
		
		if (playerMaps.size() == MAXIMAL_MAP_REGISTER_SIZE) {
		    logger.info("2 Maps have been added. Maps are getting merged...");
		    final List<Player> players = state.getStatePlayers();

		    final ServerMap firstMapEntry = playerMaps.get(players.get(0).getPlayerID());
		    ServerMap secondMapEntry = playerMaps.get(players.get(1).getPlayerID());

		    updateFortState(secondMapEntry);
		    secondMapEntry = mapSize.updateCoordinates(secondMapEntry.getMap());

		    final ServerMap combinedMap = new ServerMap();
		    combinedMap.getMap().putAll(firstMapEntry.getMap());
		    combinedMap.getMap().putAll(secondMapEntry.getMap());

		    state.setMap(combinedMap);
		}
	}

	private EMapSize chooseMapSize() {
		logger.trace("Choosing map size...");
		
		final EMapSize[] mapSizes = EMapSize.values();
		final int randomIndex = random.nextInt(mapSizes.length);
		final EMapSize randomMapSize = mapSizes[randomIndex];
		
		logger.info("The map size chosen was: {}", randomMapSize);

		return randomMapSize;
	}

	private void updateFortState(final ServerMap playerMap) {
		playerMap.getMap().values().stream().filter(tile -> tile.isFortPresent()).findFirst()
				.ifPresent(tile -> tile.setServerFortState(EServerFortState.EnemyFortPresent));
	}
}