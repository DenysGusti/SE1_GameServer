package server.rules;

import java.util.Map;

import messagesbase.UniqueGameIdentifier;
import messagesbase.UniquePlayerIdentifier;
import messagesbase.messagesfromclient.PlayerHalfMap;
import messagesbase.messagesfromclient.PlayerRegistration;
import server.exceptions.BusinessRuleViolationException;
import server.exceptions.RuleViolationException;
import server.game.Game;

public interface IRule {
	default void handleGameID(final Map<String, Game> games, final UniqueGameIdentifier gameID) throws RuleViolationException {}
	default void handleRegisterPlayer(final Map<String, Game> games, final UniqueGameIdentifier gameID,
			final PlayerRegistration playerRegistration) throws RuleViolationException {}
	default void handleHalfMap(final Map<String, Game> games, final UniqueGameIdentifier gameID, final PlayerHalfMap halfMap)
			throws BusinessRuleViolationException {}
	default void handleGameState(final Map<String, Game> games, final UniqueGameIdentifier gameID, final UniquePlayerIdentifier playerID)
			throws RuleViolationException {}
}