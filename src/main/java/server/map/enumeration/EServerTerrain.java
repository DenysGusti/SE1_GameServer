package server.map.enumeration;

public enum EServerTerrain {
	WATER(1),
	GRASS(1),
	MOUNTAIN(2);

	private final int value;

	private EServerTerrain(int value) {
		this.value = value;
	}

	public int getTerrainValue() {
		return value;
	}
}
