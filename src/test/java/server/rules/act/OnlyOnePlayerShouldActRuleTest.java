package server.rules.act;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import messagesbase.UniqueGameIdentifier;
import messagesbase.messagesfromclient.PlayerHalfMap;
import server.exceptions.BusinessRuleViolationException;
import server.game.Game;
import server.game.ServerGameState;
import server.player.Player;
import server.player.enumeration.EServerState;

public class OnlyOnePlayerShouldActRuleTest {
	@Test
	public void testHandleHalfMap_invalidTurn_ThrowsException() {
		// arrange
		String gameID = "game1";
		String playerID = "player1";
		Map<String, Game> games = new HashMap<>();
		OnlyOnePlayerShouldActRule rule = new OnlyOnePlayerShouldActRule();

		Game game = mock(Game.class);
		ServerGameState state = mock(ServerGameState.class);
		List<Player> players = new ArrayList<>();
		Player player = mock(Player.class);

		when(player.getPlayerID()).thenReturn("player1");
		when(player.getState()).thenReturn(EServerState.MUSTWAIT);
		players.add(player);

		when(state.getStatePlayers()).thenReturn(players);
		when(game.getState()).thenReturn(state);
		games.put(gameID, game);

		// act
		assertThrows(BusinessRuleViolationException.class, () -> {
			rule.handleHalfMap(games, new UniqueGameIdentifier(gameID), new PlayerHalfMap(playerID, Collections.emptyList()));
		});

		// assert
		verify(player, times(1)).getPlayerID();
		verify(player, times(1)).getState();
		verify(game, times(1)).getState();
		verify(game.getState(), times(1)).getStatePlayers();
	}

	@Test
	public void testHandleHalfMap_validTurn_DoesNotThrowException() throws BusinessRuleViolationException {
		// arrange
		String gameID = "game1";
		String playerID = "player1";
		Map<String, Game> games = new HashMap<>();
		OnlyOnePlayerShouldActRule rule = new OnlyOnePlayerShouldActRule();

		Game game = mock(Game.class);
		ServerGameState state = mock(ServerGameState.class);
		List<Player> players = new ArrayList<>();
		Player player = mock(Player.class);

		when(player.getPlayerID()).thenReturn(playerID);
		when(player.getState()).thenReturn(EServerState.MUSTACT);
		players.add(player);

		when(state.getStatePlayers()).thenReturn(players);
		when(game.getState()).thenReturn(state);
		games.put(gameID, game);

		// act
		rule.handleHalfMap(games, new UniqueGameIdentifier(gameID), new PlayerHalfMap(playerID, Collections.emptyList()));

		// assert
		verify(player, times(1)).getPlayerID();
		verify(player, times(1)).getState();
		verify(game, times(1)).getState();
		verify(game.getState(), times(1)).getStatePlayers();
	}
}
