package server.network.converter;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import messagesbase.UniquePlayerIdentifier;
import messagesbase.messagesfromclient.PlayerRegistration;
import messagesbase.messagesfromserver.EPlayerGameState;
import messagesbase.messagesfromserver.PlayerState;
import server.player.Player;

public class PlayerConverter {
	public static Player toServerPlayer(final PlayerRegistration playerRegistration, final UniquePlayerIdentifier playerID) {
		return new Player(playerRegistration.getStudentFirstName(), playerRegistration.getStudentLastName(),
				playerRegistration.getStudentUAccount(), playerID.getUniquePlayerID());
	}

	public static Collection<PlayerState> toNetworkPlayer(final List<Player> players) {
	    return players.stream()
	            .map(player -> {
	                final String firstName = player.getFirstName();
	                final String lastName = player.getLastName();
	                final String uaccount = player.getUaccount();
	                final EPlayerGameState gameState = ServerStateConverter.toNetworkPlayerGameState(player.getState());
	                final UniquePlayerIdentifier playerIdentifier = new UniquePlayerIdentifier(player.getPlayerID());
	                final boolean hasTreasure = player.isHasTreasure();

	                return new PlayerState(firstName, lastName, uaccount, gameState, playerIdentifier, hasTreasure);
	            })
	            .collect(Collectors.toSet());
	}	
}
