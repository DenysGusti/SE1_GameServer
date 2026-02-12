package server.game;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import messagesbase.UniqueGameIdentifier;
import server.map.MapHandler;
import server.map.ServerMap;
import server.player.Player;
import server.player.PlayerHandler;

public class GameManager {
	private final static Logger logger = LoggerFactory.getLogger(GameManager.class);
	private final static int MAXIMUM_RUNNING_GAMES = 99;
	private final Map<String, Game> games = new HashMap<>();
	private final Queue<String> keys = new ArrayDeque<>();

	public synchronized String createGame() {
		logger.trace("Creating new game...");
		
		if (games.size() >= MAXIMUM_RUNNING_GAMES) {
			deleteOldestGame();
		}

		String gameID = "";
		do {
			gameID = GameIDGenerator.generateGameID();
		} while (games.containsKey(gameID));

		games.put(gameID, new Game(gameID, LocalDateTime.now()));
		keys.add(gameID);

		logger.info("Created new Game with GameID: {} size {}", gameID, games.size());
		return gameID;
	}

	private void deleteOldestGame() {
		final String removedGameID = keys.poll();
		games.remove(removedGameID);

		logger.debug("Game with the GameID: {} removed", removedGameID);
	}

	public void registerPlayer(final UniqueGameIdentifier gameID, final Player player) {

		final Game game = games.get(gameID.getUniqueGameID());
		final PlayerHandler playerHandler = game.getPlayerHandler();
		final ServerGameState state = game.getState();

		playerHandler.addPlayer(player.getPlayerID(), player, state);
		logger.info("Registering Player: {} to game with GameID: {} with the state: {}", player.getPlayerID(),
				gameID.getUniqueGameID(), player.getState());
	}

	public void saveHalfMap(final UniqueGameIdentifier gameID, final ServerMap serverMap, final String playerID) {
		final Game game = games.get(gameID.getUniqueGameID());
		final MapHandler mapHandler = game.getMapHandler();
		final PlayerHandler playerHandler = game.getPlayerHandler();
		final ServerGameState state = game.getState();
		
		mapHandler.addPlayerMaps(playerID, serverMap, state);
		playerHandler.updatePlayerTurns(playerID, state);
		
		logger.info("Recieved halfmap from player with playerID: {}", playerID);
	}

	public synchronized void deleteExpiredGames() {
		final LocalDateTime now = LocalDateTime.now();
		final Duration duration = Duration.ofMinutes(10);

		final List<String> gameIDs = games.keySet().stream().filter(gameID -> {
			final Game game = games.get(gameID);
			final LocalDateTime creationTime = game.getGameIDCreationTime();
			return creationTime.plus(duration).isBefore(now);
		}).collect(Collectors.toList());

		gameIDs.stream().forEach(gameID -> games.remove(gameID));
	}
	
	public Map<String, Game> getGames() {
		return games;
	}
}
