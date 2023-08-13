package server.network.converter;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import messagesbase.messagesfromclient.PlayerHalfMap;
import messagesbase.messagesfromclient.PlayerHalfMapNode;
import server.map.Coordinate;
import server.map.ServerMap;
import server.map.ServerTile;
import server.map.enumeration.EServerFortState;
import server.map.enumeration.EServerTerrain;

public class MapConverter {
	public static ServerMap toServerMap(final PlayerHalfMap playerHalfMap) {
		final Collection<PlayerHalfMapNode> mapNodes = playerHalfMap.getMapNodes();
		final Map<Coordinate, ServerTile> map = new HashMap<>();

		for (final PlayerHalfMapNode node : mapNodes) {
			final ServerTile tile = toServerTile(node);
			final int xCoord = node.getX();
			final int yCoord = node.getY();
			final Coordinate coordinate = new Coordinate(xCoord, yCoord);
			map.put(coordinate, tile);
		}

		return new ServerMap(map);
	}
	
	private static ServerTile toServerTile(final PlayerHalfMapNode node) {
		final EServerTerrain terrain = TerrainConverter.toEServerTerrain(node.getTerrain());
		EServerFortState fortState = EServerFortState.NoOrUnknownFortState;
		
		if (node.isFortPresent()) {
			fortState = EServerFortState.MyFortPresent;
		}

		return new ServerTile(terrain, fortState);
	}
}
