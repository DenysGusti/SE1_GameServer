package server.network.converter;

import messagesbase.messagesfromclient.ETerrain;
import server.exceptions.CannotConvertEnumException;
import server.map.enumeration.EServerTerrain;

public class TerrainConverter {
	public static EServerTerrain toEServerTerrain(final ETerrain terrain) {
		switch (terrain) {
			case Grass:
				return EServerTerrain.GRASS;
			case Water:
				return EServerTerrain.WATER;
			case Mountain:
				return EServerTerrain.MOUNTAIN;
			default:
				throw new CannotConvertEnumException("Unexpected value during convertion: " + terrain);
		}
	}
	
	public static ETerrain toNetworkETerrain(final EServerTerrain terrain) {
		switch (terrain) {
			case GRASS:
				return ETerrain.Grass;
			case WATER:
				return ETerrain.Water;
			case MOUNTAIN:
				return ETerrain.Mountain;
			}
			throw new CannotConvertEnumException("Unexpected value during convertion: " + terrain);
	}
}
