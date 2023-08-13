package server.rules.act;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
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
import server.rules.act.FiveSecondsForActionRule;

public class FiveSecondsForActionRuleTest {
	@Test
	public void handleHalfMap_PlayerTakesTooLongToAct_ThrowsBusinessRuleViolationException() {
		// arrange
		FiveSecondsForActionRule rule = new FiveSecondsForActionRule();
		String gameID = "game1";
		UniqueGameIdentifier id = new UniqueGameIdentifier(gameID);
		String playerID = "player1";
		Map<String, Game> games = new HashMap<>();

		Game game = mock(Game.class);
		ServerGameState state = mock(ServerGameState.class);
		List<Player> players = new ArrayList<>();
		Player player = mock(Player.class);

		LocalDateTime now = LocalDateTime.now();
		LocalDateTime tenSecondsAgo = now.minusSeconds(10);

		when(player.getLastActionTime()).thenReturn(tenSecondsAgo);
		when(player.getState()).thenReturn(EServerState.MUSTACT);
		when(player.getPlayerID()).thenReturn(playerID);

		players.add(player);
		when(state.getStatePlayers()).thenReturn(players);
		when(game.getState()).thenReturn(state);
		games.put(gameID, game);

		// act
		assertThrows(BusinessRuleViolationException.class, () -> {
			rule.handleHalfMap(games, id, new PlayerHalfMap(playerID, Collections.emptyList()));
		});

		// assert
		verify(player, times(1)).getPlayerID();
		verify(player, times(1)).getState();
		verify(player, times(1)).getLastActionTime();
		verify(game, times(1)).getState();
		verify(game.getState(), times(1)).getStatePlayers();
	}

	@Test
	public void handleHalfMap_PlayerTakesNotTooLongToAct_DoesNotThrowException() {
		// arrange
		FiveSecondsForActionRule rule = new FiveSecondsForActionRule();
		String gameID = "game1";
		UniqueGameIdentifier id = new UniqueGameIdentifier(gameID);
		String playerID = "player1";
		Map<String, Game> games = new HashMap<>();

		Game game = mock(Game.class);
		ServerGameState state = mock(ServerGameState.class);
		List<Player> players = new ArrayList<>();
		Player player = mock(Player.class);

		LocalDateTime now = LocalDateTime.now();
		LocalDateTime tenSecondsAgo = now.minusSeconds(4);

		when(player.getLastActionTime()).thenReturn(tenSecondsAgo);
		when(player.getState()).thenReturn(EServerState.MUSTACT);
		when(player.getPlayerID()).thenReturn(playerID);

		players.add(player);
		when(state.getStatePlayers()).thenReturn(players);
		when(game.getState()).thenReturn(state);
		games.put(gameID, game);

		// act
		rule.handleHalfMap(games, id, new PlayerHalfMap(playerID, Collections.emptyList()));

		// assert
		verify(player, times(1)).getPlayerID();
		verify(player, times(1)).getState();
		verify(player, times(1)).getLastActionTime();
		verify(game, times(1)).getState();
		verify(game.getState(), times(1)).getStatePlayers();
	}
}