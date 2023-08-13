package server.network.converter;

import messagesbase.messagesfromserver.EPlayerPositionState;
import server.exceptions.CannotConvertEnumException;
import server.player.enumeration.EServerPositionState;

public class PositionStateConverter {
	public static EServerPositionState toEServerPositionState(final EPlayerPositionState positionState) {
		switch (positionState) {
			case MyPlayerPosition:
				return EServerPositionState.MyPlayerPosition;
			case EnemyPlayerPosition:
				return EServerPositionState.EnemyPlayerPosition;
			case BothPlayerPosition:
				return EServerPositionState.BothPlayerPosition;
			case NoPlayerPresent:
				return EServerPositionState.NoPlayerPresent;
			default:
				throw new CannotConvertEnumException("Unexpected value during convertion: " + positionState);
		}
	}
	
	public static EPlayerPositionState toNetworkPositionState(final EServerPositionState positionState) {
		switch (positionState) {
			case MyPlayerPosition:
				return EPlayerPositionState.MyPlayerPosition;
			case EnemyPlayerPosition:
				return EPlayerPositionState.EnemyPlayerPosition;
			case BothPlayerPosition:
				return EPlayerPositionState.BothPlayerPosition;
			case NoPlayerPresent:
				return EPlayerPositionState.NoPlayerPresent;
			default:
				throw new CannotConvertEnumException("Unexpected value during convertion: " + positionState);
		}
	}
}
