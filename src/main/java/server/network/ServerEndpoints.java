package server.network;

import jakarta.servlet.http.HttpServletResponse;

import messagesbase.messagesfromclient.PlayerHalfMap;
import messagesbase.messagesfromclient.PlayerMove;
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.CompletableFuture;

@EnableScheduling
@RestController
@RequestMapping(value = "/games")
public class ServerEndpoints {
    private final static Logger logger = LoggerFactory.getLogger(ServerEndpoints.class);

    private final GameService gameService;

    private final Path dummyClientPath;

    public ServerEndpoints(GameService gameService) {
        if (gameService == null)
            throw new IllegalArgumentException("gameService is null");

        this.gameService = gameService;

        try (InputStream jarStream = getClass().getResourceAsStream("/SE1_GameClient.jar")) {
            if (jarStream == null)
                throw new IOException("SE1_GameClient.jar not found in resources folder!");

            File tempJar = File.createTempFile("SE1_GameClient_Temp", ".jar");
            tempJar.deleteOnExit();
            Files.copy(jarStream, tempJar.toPath(), StandardCopyOption.REPLACE_EXISTING);

            dummyClientPath = tempJar.toPath();
            logger.info("Extracted dummy client to: {}", dummyClientPath.toAbsolutePath());
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    @RequestMapping(value = "", method = RequestMethod.GET, produces = MediaType.APPLICATION_XML_VALUE)
    public @ResponseBody UniqueGameIdentifier newGame(
            @RequestParam(required = false, defaultValue = "false", value = "enableDebugMode") boolean enableDebugMode,
            @RequestParam(required = false, defaultValue = "false", value = "enableDummyCompetition") boolean enableDummyCompetition) {

        UniqueGameIdentifier gameId = gameService.createGame(enableDebugMode, enableDummyCompetition);
        logger.info("Created new game with ID: {}", gameId.getUniqueGameID());

        if (enableDummyCompetition) {
            logger.info("Spawning dummy competition client for game ID: {}", gameId.getUniqueGameID());

            CompletableFuture.runAsync(() -> {
                try {
                    var processBuilder = new ProcessBuilder(
                            "java",
                            "-jar",
                            dummyClientPath.toAbsolutePath().toString(),
                            "TR",
                            "http://localhost:18235",
                            gameId.getUniqueGameID()
                    );
//                    processBuilder.inheritIO();
                    processBuilder.start();
                    logger.debug("Dummy client process started successfully.");
                } catch (IOException exception) {
                    logger.error("Failed to launch the dummy client process", exception);
                }
            });
        }

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
        return new ResponseEnvelope<>(gameState);
    }

    @RequestMapping(value = "/{gameID}/halfmaps", method = RequestMethod.POST, consumes = MediaType.APPLICATION_XML_VALUE, produces = MediaType.APPLICATION_XML_VALUE)
    public @ResponseBody ResponseEnvelope<?> receiveMap(@Validated @PathVariable UniqueGameIdentifier gameID,
                                                        @Validated @RequestBody PlayerHalfMap playerHalfMap) {
        logger.info("Received half map submission for game ID: {} from player ID: {}", gameID.getUniqueGameID(),
                playerHalfMap.getUniquePlayerID());

        gameService.submitHalfMap(gameID, playerHalfMap);
        return new ResponseEnvelope<>();
    }

    @RequestMapping(value = "/{gameID}/moves", method = RequestMethod.POST, consumes = MediaType.APPLICATION_XML_VALUE, produces = MediaType.APPLICATION_XML_VALUE)
    public @ResponseBody ResponseEnvelope<?> endGame(@Validated @PathVariable UniqueGameIdentifier gameID,
                                                     @Validated @RequestBody PlayerMove playerMove) {
        logger.info("Received move submission for game ID: {} from player ID: {}", gameID.getUniqueGameID(),
                playerMove.getUniquePlayerID());

        gameService.submitMove(gameID, playerMove);
        return new ResponseEnvelope<>();
    }
}
