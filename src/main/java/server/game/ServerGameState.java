package server.game;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import server.map.ServerMap;
import server.player.Player;
import server.player.enumeration.EServerState;

public class ServerGameState implements Cloneable {
	
	private String gameStateID;
	private List<Player> players;
	private ServerMap map;
	private final Logger logger = LoggerFactory.getLogger(ServerGameState.class);
	
	public ServerGameState() {
		this.gameStateID =  UUID.randomUUID().toString();
		this.players = new ArrayList<Player>();
		this.map = new ServerMap();
	}
	
	public ServerGameState(final String gameStateID, final List<Player> players, final ServerMap map) {
		this.gameStateID = gameStateID;
		this.players = players;
		this.map = map;
	}

	public void setMap(final ServerMap fullMap) {
		updateGameStateID();
		logger.info("New gameStateID generated because of setting map: {}", this.gameStateID);
		this.map = fullMap;	
	}

	public void setPlayers(final List<Player> players) {
		updateGameStateID();
		logger.info("New gameStateID generated because of setting player: {}", this.gameStateID);
		this.players = players;
	}

	public void updateGameStateID() {
		logger.trace("Updating gameStateID...");
		this.gameStateID = UUID.randomUUID().toString();
	}
	
	public ServerMap getMap() {
		return map;
	}

	public List<Player> getStatePlayers() {
		return players;
	}

	public String getGameStateID() {
		return gameStateID;
	}

	public void setGameOutcome() {
		logger.trace("Setting the game outcome..");
		
		players.stream().filter(act -> act.getState().equals(EServerState.MUSTACT)).findFirst()
				.ifPresent(act -> act.setState(EServerState.LOST));
		
		players.stream().filter(wait -> wait.getState().equals(EServerState.MUSTWAIT)).findFirst()
				.ifPresent(wait -> wait.setState(EServerState.WON));
		
		updateGameStateID();
		logger.info("New gameStateID generated because of setting outcome: {}", this.gameStateID);
	}
	
	@Override
	public Object clone() {
		ServerGameState clonedState = null;
	    try {
	        clonedState = (ServerGameState) super.clone();
	    } catch (CloneNotSupportedException e) {
	    	clonedState = new ServerGameState(this.gameStateID, this.players, this.map);
	    }
	    
	    clonedState = new ServerGameState();
    	clonedState.gameStateID = this.gameStateID;
        clonedState.players = new ArrayList<>();
        
        for (final Player player : this.players) {
            clonedState.players.add((Player) player.clone());
        }
        clonedState.map = (ServerMap) this.map.clone();
        
	    return clonedState;
	}
}
