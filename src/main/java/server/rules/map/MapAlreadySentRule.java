package server.rules.map;

import java.util.Map;

import messagesbase.UniqueGameIdentifier;
import messagesbase.messagesfromclient.PlayerHalfMap;
import server.exceptions.RuleViolationException;
import server.game.Game;
import server.rules.IRule;

public class MapAlreadySentRule implements IRule {
	@Override
	public void handleHalfMap(final Map<String, Game> games, final UniqueGameIdentifier gameID, final PlayerHalfMap halfMap)
			throws RuleViolationException {
		final String gameIdentifier = gameID.getUniqueGameID();
		if (games.get(gameIdentifier).getMapHandler().getPlayerMaps().containsKey(halfMap.getUniquePlayerID())) {
			throw new RuleViolationException("MapAlreadySentRule", "Map has been already sent!");
		}
	}
}

