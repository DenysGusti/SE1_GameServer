package server.map.enumeration;

import java.util.HashMap;
import java.util.Map;

import server.map.Coordinate;
import server.map.ServerMap;
import server.map.ServerTile;

public enum EMapSize {
	TENBYTEN {
		@Override
		public ServerMap updateCoordinates(final Map<Coordinate, ServerTile> map) {
			Map<Coordinate, ServerTile> updatedMap = new HashMap<>();

		    map.forEach((coordinate, tile) -> {
		        Coordinate updatedCoordinate = new Coordinate(coordinate.getX(), coordinate.getY() + Y_OFFSET);
		        ServerTile updatedTile = new ServerTile(tile.getTerrain(), tile.getTreasureState(), tile.getPositionState(), tile.getFortState());
		        updatedMap.put(updatedCoordinate, updatedTile);
		    });

		    return new ServerMap(updatedMap);
		}
	},
	TWENTYBYFIVE {
		@Override
		public ServerMap updateCoordinates(final Map<Coordinate, ServerTile> map) {
			Map<Coordinate, ServerTile> updatedMap = new HashMap<>();

		    map.forEach((coordinate, tile) -> {
		        Coordinate updatedCoordinate = new Coordinate(coordinate.getX()  + X_OFFSET, coordinate.getY());
		        ServerTile updatedTile = new ServerTile(tile.getTerrain(), tile.getTreasureState(), tile.getPositionState(), tile.getFortState());
		        updatedMap.put(updatedCoordinate, updatedTile);
		    });

		    return new ServerMap(updatedMap);
		}
	};

	private static final int Y_OFFSET = 5;
	private static final int X_OFFSET = 10;

	public abstract ServerMap updateCoordinates(final Map<Coordinate, ServerTile> map);
}
