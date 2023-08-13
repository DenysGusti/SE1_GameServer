package server.network;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import messagesbase.UniqueGameIdentifier;
import messagesbase.UniquePlayerIdentifier;
import messagesbase.messagesfromclient.PlayerHalfMap;
import messagesbase.messagesfromclient.PlayerRegistration;
import messagesbase.messagesfromserver.GameState;
import server.game.Game;
import server.game.ServerGameState;
import server.map.ServerMap;
import server.map.support.MapEditor;
import server.network.converter.Converter;
import server.player.Player;
import server.player.PlayerHandler;
import server.rules.IRule;
import server.rules.act.FiveSecondsForActionRule;
import server.rules.act.OnlyOnePlayerShouldActRule;
import server.rules.game.GameAlreadyOverRule;
import server.rules.gameid.ExistingGameIDRule;
import server.rules.map.AllCoordinatesCoveredRule;
import server.rules.map.MapAlreadySentRule;
import server.rules.map.MapTerrainsLimitRule;
import server.rules.map.MapTilesLimitRule;
import server.rules.map.NoIslandRule;
import server.rules.map.OneCastleOnGrassRule;
import server.rules.map.WaterCountAtBorderRule;
import server.rules.player.PlayerRegisteredRule;
import server.rules.player.PlayerRegistrationCountRule;

public class EndpointInputDataProcessing {
	private final static List<IRule> rules = Arrays.asList(new ExistingGameIDRule(), new PlayerRegisteredRule(),
			new PlayerRegistrationCountRule(), new GameAlreadyOverRule(), new OnlyOnePlayerShouldActRule(),
			new FiveSecondsForActionRule(), new OneCastleOnGrassRule(), new MapTerrainsLimitRule(),
			new MapTilesLimitRule(), new NoIslandRule(), new WaterCountAtBorderRule(), new AllCoordinatesCoveredRule(),
			new MapAlreadySentRule());

	public static Player createPlayerFromPlayerRegistrationInfo(final Map<String, Game> games,
			final UniqueGameIdentifier gameID, final UniquePlayerIdentifier playerID,
			final PlayerRegistration playerRegistration) {
		rules.forEach(rule -> {
			rule.handleRegisterPlayer(games, gameID, playerRegistration);
		});

		Player player = Converter.toServerPlayer(playerRegistration, playerID);

		return player;
	}

	public static ServerMap createServerMapFromPlayerHalfMap(final Map<String, Game> games,
			final UniqueGameIdentifier gameID, final PlayerHalfMap playerHalfMap) {
		rules.forEach(rule -> {
			rule.handleHalfMap(games, gameID, playerHalfMap);
		});

		ServerMap map = Converter.toServerMap(playerHalfMap);

		return map;
	}

	public static GameState procesGameState(final Map<String, Game> games,
			final UniqueGameIdentifier gameID, final UniquePlayerIdentifier playerID) {
		rules.forEach(rule -> {
			rule.handleGameState(games, gameID, playerID);
		});

		final String gameIdentifier = gameID.getUniqueGameID();
		final String playerIdentifier = playerID.getUniquePlayerID();
		final Game game = games.get(gameIdentifier);
		final PlayerHandler playerHandler = game.getPlayerHandler();
		final ServerGameState state = game.getState();
		final List<Player> anonymizedPlayers = playerHandler.anonymizePlayerID(playerIdentifier);

		if (game.getMapHandler().getPlayerMaps().isEmpty()) {
			return new GameState(Converter.toPlayerState(anonymizedPlayers), state.getGameStateID());
		}

		final ServerMap modifiedServerMap = MapEditor.editMapForPlayer(state, playerIdentifier);
        GameState gameState = Converter.toGameState(modifiedServerMap, anonymizedPlayers, state.getGameStateID());

		return gameState;
	}
}
