package server.map.support;

import java.util.Map;
import java.util.Optional;
import java.util.Random;

import server.map.Coordinate;
import server.map.ServerMap;
import server.map.enumeration.EServerFortState;
import server.player.enumeration.EServerPositionState;

public class PlayerStateManager {
	public static void initializePlayerState(final ServerMap map, final int index) {
		final EServerFortState myFortState = (index == 0) ? EServerFortState.MyFortPresent
				: EServerFortState.EnemyFortPresent;
		final EServerFortState opponentFortState = (index == 0) ? EServerFortState.EnemyFortPresent
				: EServerFortState.MyFortPresent;
		// Coordinate currentPlayer
		final Optional<Coordinate> myPlayerCoord = map.getMap().entrySet().stream()
				.filter(tile -> tile.getValue().getFortState() == myFortState).map(Map.Entry::getKey).findFirst();

		// Coordinate enemyPlayer
		final Optional<Coordinate> enemyPlayerCoord = map.getMap().entrySet().stream()
				.filter(tile -> tile.getValue().getFortState() == opponentFortState).map(Map.Entry::getKey).findFirst();

		setMyPlayerState(map, myPlayerCoord);
		setEnemyPlayerState(map, enemyPlayerCoord);

	}

	private static void setMyPlayerState(final ServerMap map, final Optional<Coordinate> myPlayerCoord) {
		myPlayerCoord.ifPresent(coord -> {
			// Set Fort at coordinate currentPlayer to MyFortPosition
			map.getMap().get(coord).setServerFortState(EServerFortState.MyFortPresent);
			// Set serverPositionState to MyPlayerPosition at coordinate currentPlayer
			map.getMap().get(coord).setServerPositionState(EServerPositionState.MyPlayerPosition);
		});
	}

	private static void setEnemyPlayerState(final ServerMap map, final Optional<Coordinate> enemyPlayerCoord) {
		enemyPlayerCoord.ifPresent(coord -> {
			// Set Fort at coordinate enemyPlayer to NoOrUnknownFort
			map.getMap().get(coord).setServerFortState(EServerFortState.NoOrUnknownFortState);
			// Find newEnemyPlayer coordinate so that it is random and != enemyPlayer
			// coordinate
			Coordinate newEnemyPlayerCoord;
			do {
				newEnemyPlayerCoord = map.getMap().keySet().stream().skip(new Random().nextInt(map.getMap().size()))
						.findFirst().get();
			} while (newEnemyPlayerCoord.equals(coord));
			// Set serverPositionState at newEnemyPlayer to either EnemeyPlayerPosition
			// or BothPlayerPosition
			EServerPositionState newEnemyPlayerPosition;
			if (map.getMap().get(newEnemyPlayerCoord).getPositionState() == EServerPositionState.MyPlayerPosition) {
				newEnemyPlayerPosition = EServerPositionState.BothPlayerPosition;
			} else {
				newEnemyPlayerPosition = EServerPositionState.EnemyPlayerPosition;
			}
			map.getMap().get(newEnemyPlayerCoord).setServerPositionState(newEnemyPlayerPosition);
		});
	}
}
