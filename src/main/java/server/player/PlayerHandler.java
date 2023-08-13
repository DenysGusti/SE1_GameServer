package server.player;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import server.game.ServerGameState;
import server.player.enumeration.EServerState;

public class PlayerHandler {
	private final Logger logger = LoggerFactory.getLogger(PlayerHandler.class);
	private final Map<String, Player> players;
	private final Random random = new Random();
	private final List<Player> orderedPlayer;
	private final static int MAXIMAL_PLAYER_REGISTER_SIZE = 2;
	
	public PlayerHandler() {
		players = new HashMap<String, Player>();
		orderedPlayer = new ArrayList<Player>();
	}
	
	public Map<String, Player> getPlayers() {
		return players;
	}

	public void addPlayer(final String playerID, final Player player, final ServerGameState state) {
		logger.info("Adding player {}: to game", playerID);
		
		players.put(playerID, player);
		player.setState(EServerState.MUSTWAIT);
		
		if (players.size() == MAXIMAL_PLAYER_REGISTER_SIZE) {
			logger.info("2 Players have been registered...");
			setFirstPlayerTurn();
			state.setPlayers(orderedPlayer);
		}	
	}
	
	private void setFirstPlayerTurn() {
		logger.trace("Setting first turns for players...");
	    final Player[] players = this.players.values().stream().toArray(Player[]::new);
	    final int randomID = random.nextInt(players.length);

	    for (int iteration = 0; iteration < players.length; iteration++) {
	        final Player eachPlayer = players[iteration];
	        
	        if (iteration == randomID) {
	        	eachPlayer.setState(EServerState.MUSTACT);
	        	eachPlayer.performAction(LocalDateTime.now());
	            logger.info("Player active is {}", eachPlayer.getPlayerID());
	        } else {
	        	eachPlayer.setState(EServerState.MUSTWAIT);
	        	eachPlayer.performAction(LocalDateTime.now());
	            logger.info("Player inactive is {}", eachPlayer.getPlayerID());
	        }
	    }
	    
	    final int randomIDForOrder = random.nextInt(players.length);
		orderedPlayer.add(players[randomIDForOrder]);
		orderedPlayer.add(players[(randomIDForOrder + 1) % 2]);

	}

	public void updatePlayerTurns(final String playerID, ServerGameState state) {
	    logger.trace("Switching turns...");

	    players.values().forEach(player -> {
	        if (player.getPlayerID().equals(playerID)) {
	            player.setState(EServerState.MUSTWAIT);
	            logger.info(player.getPlayerID() + " " + player.getState());
	        } else {
	            player.setState(EServerState.MUSTACT);
	            player.performAction(LocalDateTime.now());
	            logger.info(player.getPlayerID() + " " + player.getState());
	        }
	    });

	    state.updateGameStateID();
	}
	
	public List<Player> anonymizePlayerID(final String playerID) {
	    logger.trace("GameState is being requested, anonymizing other players' IDs...");

	    final List<Player> copiedPlayers = new ArrayList<>();

	    for (final Player player : players.values()) {
	        final Player copiedPlayer = (Player) player.clone();
	        final String newID = player.getPlayerID().equals(playerID) ? player.getPlayerID() : UUID.randomUUID().toString();
	        copiedPlayer.setPlayerID(newID);
	        copiedPlayers.add(copiedPlayer);
	    }

	    return copiedPlayers;
	}
	
	public List<Player> getOrderedPlayer() {
		return orderedPlayer;
	}
}
