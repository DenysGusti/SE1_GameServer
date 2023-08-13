package server.map;

import server.map.enumeration.EServerFortState;
import server.map.enumeration.EServerTerrain;
import server.map.enumeration.EServerTreasureState;
import server.player.enumeration.EServerPositionState;

public class ServerTile implements Cloneable {
	private final EServerTerrain terrain;
	private EServerTreasureState treasureState;
	private EServerPositionState positionState;
	private EServerFortState fortState;

	public ServerTile(final EServerTerrain terrain, final EServerFortState fortState) {
		this.terrain = terrain;
		this.treasureState = EServerTreasureState.NoOrUnknownTreasureState;
		this.positionState = EServerPositionState.NoPlayerPresent;
		this.fortState = fortState;
	}

	public ServerTile(final EServerTerrain terrain, final EServerTreasureState treasureState,
			final EServerPositionState positionState, final EServerFortState fortState) {
		this.terrain = terrain;
		this.treasureState = treasureState;
		this.positionState = positionState;
		this.fortState = fortState;
	}
	
	public ServerTile(final ServerTile tile) {
		this.terrain = tile.terrain;
		this.treasureState = tile.treasureState;
		this.positionState = tile.positionState;
		this.fortState = tile.fortState;
	}

	public EServerTerrain getTerrain() {
		return terrain;
	}

	public EServerFortState getFortState() {
		return fortState;
	}
	
	public boolean isFortPresent() {
		return fortState == EServerFortState.MyFortPresent;
	}
	
	public boolean isEnemyFortPresent() {
		return fortState == EServerFortState.EnemyFortPresent;
	}
	
	public boolean isTreasurePresent() {
		return treasureState == EServerTreasureState.MyTreasureIsPresent;
	}

	public boolean isEnemyPresent() {
		return (positionState == EServerPositionState.EnemyPlayerPosition
				|| positionState == EServerPositionState.BothPlayerPosition);
	}

	public EServerTreasureState getTreasureState() {
		return treasureState;
	}

	public EServerPositionState getPositionState() {
		return positionState;
	}

	public void setServerFortState(final EServerFortState fortState) {
		this.fortState = fortState;
	}
	
	public void setServerPositionState(final EServerPositionState positionState) {
		this.positionState = positionState;
	}

	public void setServerTreasureState(final EServerTreasureState treasureState) {
		this.treasureState = treasureState;
	}
	
	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			return new ServerTile(this.terrain, this.treasureState, this.positionState, this.fortState);
		}
	}
}
