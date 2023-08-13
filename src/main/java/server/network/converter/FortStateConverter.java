package server.network.converter;

import messagesbase.messagesfromserver.EFortState;
import server.exceptions.CannotConvertEnumException;
import server.map.enumeration.EServerFortState;

public class FortStateConverter {
	public static EServerFortState toEServerFortState(final EFortState fortState) {
		switch (fortState) {
			case EnemyFortPresent:
				return EServerFortState.EnemyFortPresent;
			case MyFortPresent:
				return EServerFortState.MyFortPresent;
			case NoOrUnknownFortState:
				return EServerFortState.NoOrUnknownFortState;
			default:
				throw new CannotConvertEnumException("Unexpected value during convertion: " + fortState);
			}
	}
	
	public static EFortState toNetworkFortState(final EServerFortState fortState) {
		switch (fortState) {
			case EnemyFortPresent:
				return EFortState.EnemyFortPresent;
			case MyFortPresent:
				return EFortState.MyFortPresent;
			case NoOrUnknownFortState:
				return EFortState.NoOrUnknownFortState;
			default:
				throw new CannotConvertEnumException("Unexpected value during convertion: " + fortState);
			}
	}
}
