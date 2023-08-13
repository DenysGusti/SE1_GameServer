package server.network.converter;

import messagesbase.messagesfromserver.EPlayerGameState;
import server.exceptions.CannotConvertEnumException;
import server.player.enumeration.EServerState;

public class ServerStateConverter {
	public static EServerState toEServerState(final EPlayerGameState ePlayerGameState) {
		switch (ePlayerGameState) {
			case Won:
				return EServerState.WON;
			case Lost:
				return EServerState.LOST;
			case MustWait:
				return EServerState.MUSTWAIT;
			case MustAct:
				return EServerState.MUSTACT;
		}
		throw new CannotConvertEnumException("Convert EPlayerGameState to EServerState failed!");
	}
	
	public static EPlayerGameState toNetworkPlayerGameState(final EServerState serverState) {
		switch (serverState) {
			case WON:
				return EPlayerGameState.Won;
			case LOST:
				return EPlayerGameState.Lost;
			case MUSTWAIT:
				return EPlayerGameState.MustWait;
			case MUSTACT:
				return EPlayerGameState.MustAct;
		}
		throw new CannotConvertEnumException("Convert EServerState to EPlayerGameState failed!");
	}
}
