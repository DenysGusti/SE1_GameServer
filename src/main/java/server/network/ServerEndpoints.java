package server.network;

import java.util.UUID;

import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import messagesbase.ResponseEnvelope;
import messagesbase.UniqueGameIdentifier;
import messagesbase.UniquePlayerIdentifier;
import messagesbase.messagesfromclient.PlayerHalfMap;
import messagesbase.messagesfromclient.PlayerRegistration;
import messagesbase.messagesfromserver.GameState;
import server.exceptions.GenericExampleException;
import server.game.GameManager;
import server.map.ServerMap;
import server.network.fromclient.FromClientConverter;
import server.player.Player;
import server.services.GameService;

@EnableScheduling
@RestController
@RequestMapping(value = "/games")
public class ServerEndpoints {
    private final static Logger logger = LoggerFactory.getLogger(ServerEndpoints.class);

    private final GameService gameService;

    public ServerEndpoints(GameService gameService) {
        this.gameService = gameService;
    }

    @RequestMapping(value = "", method = RequestMethod.GET, produces = MediaType.APPLICATION_XML_VALUE)
    public @ResponseBody UniqueGameIdentifier newGame(
            @RequestParam(required = false, defaultValue = "false", value = "enableDebugMode") boolean enableDebugMode,
            @RequestParam(required = false, defaultValue = "false", value = "enableDummyCompetition") boolean enableDummyCompetition) {

        UniqueGameIdentifier gameId = gameService.createGame(enableDebugMode, enableDummyCompetition);
        logger.info("Created new game with ID: {}", gameId.getUniqueGameID());

        return gameId;
    }

    @Scheduled(fixedRate = 5000)
    public void removeOldGames() {
        logger.debug("Removing old games");
        gameService.removeOldGames();
    }

//	@RequestMapping(value = "/{gameID}/players", method = RequestMethod.POST, consumes = MediaType.APPLICATION_XML_VALUE, produces = MediaType.APPLICATION_XML_VALUE)
//	public @ResponseBody ResponseEnvelope<UniquePlayerIdentifier> registerPlayer(
//			@Validated @PathVariable UniqueGameIdentifier gameID,
//			@Validated @RequestBody PlayerRegistration playerRegistration) {
//		UniquePlayerIdentifier newPlayerID = new UniquePlayerIdentifier(UUID.randomUUID().toString());
//
//		final Player player = EndpointInputDataProcessing.createPlayerFromPlayerRegistrationInfo(gameManager.getGames(), gameID, newPlayerID, playerRegistration);
//		gameManager.registerPlayer(gameID, player);
//
//		ResponseEnvelope<UniquePlayerIdentifier> playerIDMessage = new ResponseEnvelope<>(newPlayerID);
//		return playerIDMessage;
//	}
//
//	@RequestMapping(value = "/{gameID}/halfmaps", method = RequestMethod.POST, consumes = MediaType.APPLICATION_XML_VALUE, produces = MediaType.APPLICATION_XML_VALUE)
//	public @ResponseBody ResponseEnvelope<?> receiveMap(@Validated @PathVariable UniqueGameIdentifier gameID,
//			@Validated @RequestBody PlayerHalfMap playerHalfMap) {
//
//		final ServerMap map = EndpointInputDataProcessing.createServerMapFromPlayerHalfMap(gameManager.getGames(), gameID, playerHalfMap);
//		gameManager.saveHalfMap(gameID, map, playerHalfMap.getUniquePlayerID());
//
//		return new ResponseEnvelope<>();
//	}
//
//	@RequestMapping(value = "/{gameID}/states/{playerID}", method = RequestMethod.GET, produces = MediaType.APPLICATION_XML_VALUE)
//	public @ResponseBody ResponseEnvelope<?> sendGameState(@Validated @PathVariable UniqueGameIdentifier gameID,
//			@Validated @PathVariable UniquePlayerIdentifier playerID) {
//
//		GameState gameState = EndpointInputDataProcessing.procesGameState(gameManager.getGames(), gameID, playerID);
//		ResponseEnvelope<GameState> result = new ResponseEnvelope<>(gameState);
//
//		return result;
//	}
//
//	@ExceptionHandler({ GenericExampleException.class })
//	public @ResponseBody ResponseEnvelope<?> handleException(GenericExampleException ex, HttpServletResponse response) {
//		ResponseEnvelope<?> result = new ResponseEnvelope<>(ex.getErrorName(), ex.getMessage());
//		ex.processGameState(gameManager);
//
//		response.setStatus(HttpServletResponse.SC_OK);
//		return result;
//	}
}
