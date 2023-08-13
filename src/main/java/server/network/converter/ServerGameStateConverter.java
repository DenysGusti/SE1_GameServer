package server.network.converter;

import java.util.List;

import messagesbase.messagesfromserver.GameState;
import server.map.ServerMap;
import server.player.Player;

public class ServerGameStateConverter {
	public static GameState toNetworkGameState(final ServerMap serverMap, final List<Player> players,
			final String gameStateID) {
		return new GameState(FullMapConverter.toFullMap(serverMap), PlayerConverter.toNetworkPlayer(players),
				gameStateID);
	}
}
