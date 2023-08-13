package server.network.converter;

import java.util.Collection;
import java.util.List;

import messagesbase.UniquePlayerIdentifier;
import messagesbase.messagesfromclient.PlayerHalfMap;
import messagesbase.messagesfromclient.PlayerRegistration;
import messagesbase.messagesfromserver.GameState;
import messagesbase.messagesfromserver.PlayerState;
import server.map.ServerMap;
import server.player.Player;

public class Converter {
	public static Player toServerPlayer(final PlayerRegistration player, final UniquePlayerIdentifier playerID) {
		return PlayerConverter.toServerPlayer(player, playerID);
	}
	
	public static ServerMap toServerMap(final PlayerHalfMap playerHalfMap) {
		return MapConverter.toServerMap(playerHalfMap);
	}
	
	public static GameState toGameState(final ServerMap serverMap, final List<Player> players, final String gameStateID) {
		return ServerGameStateConverter.toNetworkGameState(serverMap, players, gameStateID);
	}

	public static Collection<PlayerState> toPlayerState(final List<Player> players) {
		return PlayerConverter.toNetworkPlayer(players);
	}
}
