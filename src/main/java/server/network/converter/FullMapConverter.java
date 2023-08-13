package server.network.converter;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import messagesbase.messagesfromclient.ETerrain;
import messagesbase.messagesfromserver.EFortState;
import messagesbase.messagesfromserver.EPlayerPositionState;
import messagesbase.messagesfromserver.ETreasureState;
import messagesbase.messagesfromserver.FullMap;
import messagesbase.messagesfromserver.FullMapNode;
import server.map.Coordinate;
import server.map.ServerMap;
import server.map.ServerTile;
import server.map.enumeration.EServerFortState;
import server.map.enumeration.EServerTerrain;
import server.map.enumeration.EServerTreasureState;
import server.player.enumeration.EServerPositionState;

public class FullMapConverter {
	public static ServerMap toFullServerMap(final ServerMap serverMap) {
		final Collection<FullMapNode> mapNodes = new HashSet<>();
		final Map<Coordinate, ServerTile> map = new HashMap<>();

		for (final FullMapNode node : mapNodes) {
			final ServerTile tile = toServerTile(node);
			final int xCoord = node.getX();
			final int yCoord = node.getY();
			final Coordinate coordinate = new Coordinate(xCoord, yCoord);
			map.put(coordinate, tile);
		}

		return new ServerMap(map);
	}
	
	public static FullMap toFullMap(final ServerMap serverMap) {
		final Collection<FullMapNode> mapNodes = new HashSet<>();
		for (final Entry<Coordinate, ServerTile> node : serverMap.getMap().entrySet()) {
			mapNodes.add(toFullMapNode(node));
		}
		return new FullMap(mapNodes);
	}
	
	private static FullMapNode toFullMapNode(final Entry<Coordinate, ServerTile> node) {
		final ETerrain terrain = TerrainConverter.toNetworkETerrain(node.getValue().getTerrain());
		final EFortState fortState = FortStateConverter.toNetworkFortState(node.getValue().getFortState());
		final EPlayerPositionState positionState = PositionStateConverter.toNetworkPositionState(node.getValue().getPositionState());
		final ETreasureState treasureState = TreasureStateConverter.toNetworkTreasureState(node.getValue().getTreasureState());
		final int xCoord = node.getKey().getX();
		final int yCoord = node.getKey().getY();

		return new FullMapNode(terrain, positionState, treasureState, fortState, xCoord, yCoord);
	}
	
	private static ServerTile toServerTile(final FullMapNode node) {
		final EServerTerrain terrain = TerrainConverter.toEServerTerrain(node.getTerrain());
		final EServerFortState fortState = FortStateConverter.toEServerFortState(node.getFortState());
		final EServerPositionState positionState = PositionStateConverter.toEServerPositionState(node.getPlayerPositionState());
		final EServerTreasureState treasureState = TreasureStateConverter.toEServerTreasureState(node.getTreasureState());

		return new ServerTile(terrain, treasureState, positionState, fortState);
	}
}
