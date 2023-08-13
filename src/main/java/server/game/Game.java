package server.game;

import java.time.LocalDateTime;

import server.map.MapHandler;
import server.player.PlayerHandler;

public class Game {
	private final String gameID;
	private final LocalDateTime gameIDCreationTime;
	private final PlayerHandler playerHandler;
	private final MapHandler mapHandler;
	private final ServerGameState state;
	
	public Game(final String gameID, final LocalDateTime gameIDCreationTime) {
		this.gameID = gameID;
		this.gameIDCreationTime = gameIDCreationTime;
		this.playerHandler = new PlayerHandler();
		this.mapHandler = new MapHandler();
		this.state = new ServerGameState();
	}

	public String getGameID() {
		return gameID;
	}
	
	public LocalDateTime getGameIDCreationTime() {
		return gameIDCreationTime;
	}

	public ServerGameState getState() {
		return state;
	}

	public PlayerHandler getPlayerHandler() {
		return playerHandler;
	}

	public MapHandler getMapHandler() {
		return mapHandler;
	}
}
