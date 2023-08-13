package server.network.converter;

import messagesbase.messagesfromserver.ETreasureState;
import server.exceptions.CannotConvertEnumException;
import server.map.enumeration.EServerTreasureState;

public class TreasureStateConverter {
	public static EServerTreasureState toEServerTreasureState(final ETreasureState treasureState) {
		switch (treasureState) {
			case MyTreasureIsPresent:
				return EServerTreasureState.MyTreasureIsPresent;
			case NoOrUnknownTreasureState:
				return EServerTreasureState.NoOrUnknownTreasureState;
			default:
				throw new CannotConvertEnumException("Unexpected value during convertion: " + treasureState);
			}
	}
	
	public static ETreasureState toNetworkTreasureState(final EServerTreasureState treasureState) {
		switch (treasureState) {
			case MyTreasureIsPresent:
				return ETreasureState.MyTreasureIsPresent;
			case NoOrUnknownTreasureState:
				return ETreasureState.NoOrUnknownTreasureState;
			default:
				throw new CannotConvertEnumException("Unexpected value during convertion: " + treasureState);
			}
	}
}
