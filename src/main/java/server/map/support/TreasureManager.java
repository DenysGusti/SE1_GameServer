package server.map.support;

import java.util.Random;

import server.map.ServerMap;
import server.map.enumeration.EServerTerrain;
import server.map.enumeration.EServerTreasureState;

public class TreasureManager {
	public static void setTreasureState(final ServerMap map) {
		if (map.getMap().size() == 0) {
			return;
		}

		map.getMap().values().stream()
				.filter(tile -> (tile.getTerrain() == EServerTerrain.GRASS && !tile.isFortPresent()))
				.skip(new Random().nextInt(map.getMap().size())).findFirst()
				.ifPresent(tile -> tile.setServerTreasureState(EServerTreasureState.MyTreasureIsPresent));
	}
	
	public static void hideTreasure(final ServerMap map) {
		map.getMap().entrySet().stream()
				.filter(tile -> tile.getValue().isTreasurePresent())
				.forEach(tile -> tile.getValue().setServerTreasureState(EServerTreasureState.NoOrUnknownTreasureState));
	}
}
