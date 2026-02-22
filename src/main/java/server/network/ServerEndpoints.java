package server.network;

import jakarta.servlet.http.HttpServletResponse;

import messagesbase.messagesfromclient.PlayerHalfMap;
import messagesbase.messagesfromserver.GameState;
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
import messagesbase.messagesfromclient.PlayerRegistration;
import server.exception.GenericServerException;
import server.service.GameService;

@EnableScheduling
@RestController
@RequestMapping(value = "/games")
public class ServerEndpoints {
    private final static Logger logger = LoggerFactory.getLogger(ServerEndpoints.class);

    private final GameService gameService;

    public ServerEndpoints(GameService gameService) {
        if (gameService == null)
            throw new IllegalArgumentException("gameService is null");

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

    @ExceptionHandler({GenericServerException.class})
    public @ResponseBody ResponseEnvelope<?> handleException(GenericServerException genericServerException,
                                                             HttpServletResponse httpServletResponse) {
        logger.warn("Business rule violation: {}", genericServerException.getClass().getSimpleName());
        httpServletResponse.setStatus(HttpServletResponse.SC_OK);
        return new ResponseEnvelope<>(genericServerException.getClass().getSimpleName(), genericServerException.getMessage());
    }

    @RequestMapping(value = "/{gameID}/players", method = RequestMethod.POST, consumes = MediaType.APPLICATION_XML_VALUE, produces = MediaType.APPLICATION_XML_VALUE)
    public @ResponseBody ResponseEnvelope<UniquePlayerIdentifier> registerPlayer(
            @Validated @PathVariable UniqueGameIdentifier gameID,
            @Validated @RequestBody PlayerRegistration playerRegistration) {

        logger.info("Received player registration attempt from {} for game ID: {}",
                playerRegistration.getStudentUAccount(), gameID.getUniqueGameID());

        UniquePlayerIdentifier playerId = gameService.registerPlayer(gameID, playerRegistration);

        logger.debug("Successfully registered player {} in game ID: {} with player ID: {}",
                playerRegistration.getStudentUAccount(), gameID.getUniqueGameID(), playerId.getUniquePlayerID());
        return new ResponseEnvelope<>(playerId);
    }

    @RequestMapping(value = "/{gameID}/states/{playerID}", method = RequestMethod.GET, produces = MediaType.APPLICATION_XML_VALUE)
    public @ResponseBody ResponseEnvelope<?> sendGameState(@Validated @PathVariable UniqueGameIdentifier gameID,
                                                           @Validated @PathVariable UniquePlayerIdentifier playerID) {
        logger.info("Received game state request for game ID: {} from player ID: {}",
                gameID.getUniqueGameID(), playerID.getUniquePlayerID());

        GameState gameState = gameService.getGameState(gameID, playerID);

        logger.debug("Successfully generated game state for game ID: {} with ID: {}",
                gameID.getUniqueGameID(), gameState.getGameStateId());
        logger.trace("Game state: {}", gameState.getPlayers());
        return new ResponseEnvelope<>(gameState);
    }


    @RequestMapping(value = "/{gameID}/halfmaps", method = RequestMethod.POST, consumes = MediaType.APPLICATION_XML_VALUE, produces = MediaType.APPLICATION_XML_VALUE)
    public @ResponseBody ResponseEnvelope<?> receiveMap(@Validated @PathVariable UniqueGameIdentifier gameID,
                                                        @Validated @RequestBody PlayerHalfMap playerHalfMap) {
        logger.info("Received half map submission for game ID: {} from player ID: {}", gameID.getUniqueGameID(),
                playerHalfMap.getUniquePlayerID());

        gameService.submitHalfMap(gameID, playerHalfMap);

        logger.debug("Successfully accepted half map for game ID: {}", gameID.getUniqueGameID());
        return new ResponseEnvelope<>();
    }
}
