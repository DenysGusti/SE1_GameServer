package server.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import messagesbase.*;
import messagesbase.messagesfromclient.*;
import messagesbase.messagesfromserver.*;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import server.converter.HalfMapConverter;
import server.data.HalfMap;
import server.data.XYPair;
import server.entity.*;
import server.exception.*;
import server.repository.*;
import server.validation.HalfMapValidator;
import server.validation.Notification;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.random.RandomGenerator;

@Service
public class GameService {
    private static final Logger logger = LoggerFactory.getLogger(GameService.class);

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int GAME_ID_LENGTH = 5;

    private static final int MAX_GAMES = 5;
    private static final int PLAYERS_PER_GAME = 2;
    private static final int MAX_GAMES_PER_PLAYER = 10;

    private static final Duration GAME_LIFETIME = Duration.ofMinutes(3);
    //    private static final Duration MINIMUM_POLLING_INTERVAL = Duration.ofMillis(300);
    private static final Duration MAXIMUM_ACTION_INTERVAL = Duration.ofMillis(5500);

    private final RandomGenerator randomGenerator;
    private final HalfMapValidator halfMapValidator;
    private final HalfMapConverter halfMapConverter;

    private final CommandTimeRepository commandTimeRepository;
    private final FullMapNodeRepository fullMapNodeRepository;
    private final GameRepository gameRepository;
    private final GameStateRepository gameStateRepository;
    private final PlayerFullMapRepository playerFullMapRepository;
    private final PlayerParticipationRepository playerParticipationRepository;
    private final PlayerRegistrationRepository playerRegistrationRepository;
    private final PlayerRoundRepository playerRoundRepository;
    private final PlayerStateRepository playerStateRepository;
    private final QueryTimeRepository queryTimeRepository;

    public GameService(RandomGenerator randomGenerator,
                       HalfMapValidator halfMapValidator,
                       HalfMapConverter halfMapConverter,
                       CommandTimeRepository commandTimeRepository,
                       FullMapNodeRepository fullMapNodeRepository,
                       GameRepository gameRepository,
                       GameStateRepository gameStateRepository,
                       PlayerFullMapRepository playerFullMapRepository,
                       PlayerParticipationRepository playerParticipationRepository,
                       PlayerRegistrationRepository playerRegistrationRepository,
                       PlayerRoundRepository playerRoundRepository,
                       PlayerStateRepository playerStateRepository,
                       QueryTimeRepository queryTimeRepository
    ) {
        if (randomGenerator == null)
            throw new IllegalArgumentException("randomGenerator is null");
        if (halfMapValidator == null)
            throw new IllegalArgumentException("halfMapValidator is null");
        if (halfMapConverter == null)
            throw new IllegalArgumentException("halfMapConverter is null");
        if (commandTimeRepository == null)
            throw new IllegalArgumentException("commandTimeRepository is null");
        if (fullMapNodeRepository == null)
            throw new IllegalArgumentException("fullMapNodeRepository is null");
        if (gameRepository == null)
            throw new IllegalArgumentException("gameRepository is null");
        if (gameStateRepository == null)
            throw new IllegalArgumentException("gameStateRepository is null");
        if (playerFullMapRepository == null)
            throw new IllegalArgumentException("playerFullMapRepository is null");
        if (playerParticipationRepository == null)
            throw new IllegalArgumentException("playerParticipationRepository is null");
        if (playerRegistrationRepository == null)
            throw new IllegalArgumentException("playerRegistrationRepository is null");
        if (playerRoundRepository == null)
            throw new IllegalArgumentException("playerRoundRepository is null");
        if (playerStateRepository == null)
            throw new IllegalArgumentException("playerStateRepository is null");
        if (queryTimeRepository == null)
            throw new IllegalArgumentException("queryTimeRepository is null");

        this.randomGenerator = randomGenerator;
        this.halfMapValidator = halfMapValidator;
        this.halfMapConverter = halfMapConverter;
        this.commandTimeRepository = commandTimeRepository;
        this.fullMapNodeRepository = fullMapNodeRepository;
        this.gameRepository = gameRepository;
        this.gameStateRepository = gameStateRepository;
        this.playerFullMapRepository = playerFullMapRepository;
        this.playerParticipationRepository = playerParticipationRepository;
        this.playerRegistrationRepository = playerRegistrationRepository;
        this.playerRoundRepository = playerRoundRepository;
        this.playerStateRepository = playerStateRepository;
        this.queryTimeRepository = queryTimeRepository;
    }

    @Transactional
    public UniqueGameIdentifier createGame(boolean debugMode, boolean dummyCompetition) {
        String newGameId;
        do {
            var sb = new StringBuilder(GAME_ID_LENGTH);
            for (int i = 0; i < GAME_ID_LENGTH; ++i)
                sb.append(CHARACTERS.charAt(randomGenerator.nextInt(CHARACTERS.length())));

            newGameId = sb.toString();
        } while (gameRepository.existsById(newGameId));

        boolean horizontalFullMap = randomGenerator.nextBoolean();
        boolean firstPlayerTopOrLeftSide = randomGenerator.nextBoolean();

        var game = new GameEntity(newGameId, debugMode, dummyCompetition, horizontalFullMap, firstPlayerTopOrLeftSide, LocalDateTime.now());
        gameRepository.save(game);

        return new UniqueGameIdentifier(newGameId);
    }

    @Transactional
    public void removeOldGames() {
        LocalDateTime tenMinutesAgo = LocalDateTime.now().minus(GAME_LIFETIME);
        gameRepository.deleteByCreatedAtBefore(tenMinutesAgo);

        long totalGames = gameRepository.count();
        if (totalGames > MAX_GAMES) {
            List<GameEntity> allGames = gameRepository.findAllByOrderByCreatedAtAsc();

            int excessGames = (int) (totalGames - MAX_GAMES);

            for (int i = 0; i < excessGames; ++i) {
                GameEntity oldestGame = allGames.get(i);
                gameRepository.delete(oldestGame);
                logger.info("Deleted excess game to maintain {}-game limit: {}", MAX_GAMES, oldestGame.getId());
            }
        }

        playerRegistrationRepository.deleteOrphanedPlayerRegistrations();
    }

    @Transactional
    public UniquePlayerIdentifier registerPlayer(UniqueGameIdentifier uniqueGameIdentifier,
                                                 PlayerRegistration playerRegistration) {
        if (playerRegistration == null)
            throw new IllegalArgumentException("playerRegistration is null");
        if (uniqueGameIdentifier == null)
            throw new IllegalArgumentException("uniqueGameIdentifier is null");

        String uAccount = playerRegistration.getStudentUAccount();
        // optional to restrict players
        if (!playerRegistrationRepository.existsById(uAccount)) {
            logger.info("New player tried to join game with unknown uAccount: {}", uAccount);
//            throw new UnknownUAccountException(uAccount);
        }

        String gameId = uniqueGameIdentifier.getUniqueGameID();
        GameEntity game = gameRepository.findById(gameId)
                .orElseThrow(() -> new MatchNotFoundException(gameId));

        long currentPlayers = playerParticipationRepository.countByGameId(gameId);
        if (currentPlayers >= PLAYERS_PER_GAME)
            throw new PlayerRegisterRuleException();

        long activeGamesForPlayer = playerParticipationRepository.countByPlayerRegistration_uAccount(uAccount);
        if (activeGamesForPlayer >= MAX_GAMES_PER_PLAYER)
            throw new ManualGameCreationOveruseException();

        var player = playerRegistrationRepository.findById(uAccount)
                .orElseGet(() -> new PlayerRegistrationEntity(
                        uAccount,
                        playerRegistration.getStudentFirstName(),
                        playerRegistration.getStudentLastName()
                ));
        player = playerRegistrationRepository.save(player);

        boolean isEvenTurn;
        PlayerParticipationEntity firstPlayerParticipation = null;
        if (currentPlayers == 0)
            isEvenTurn = randomGenerator.nextBoolean();
        else {
            firstPlayerParticipation = playerParticipationRepository.findByGameId(gameId).getFirst();
            isEvenTurn = !firstPlayerParticipation.isFirstTurn();
        }

        String fakePlayerId;
        do {
            fakePlayerId = java.util.UUID.randomUUID().toString();
        } while (playerParticipationRepository.existsById(fakePlayerId) ||
                playerParticipationRepository.existsByFakePlayerId(fakePlayerId));

        var playerParticipation = new PlayerParticipationEntity(fakePlayerId, player, game, isEvenTurn);
        playerParticipation = playerParticipationRepository.save(playerParticipation);

        if (currentPlayers == 0)
            createFirstGameState(playerParticipation, game);
        else
            createSecondGameState(firstPlayerParticipation, playerParticipation, game);

        return new UniquePlayerIdentifier(playerParticipation.getPlayerId());
    }

    private void createFirstGameState(PlayerParticipationEntity playerParticipation, GameEntity game) {
        var gameState = new GameStateEntity(game, 0, null);
        gameStateRepository.save(gameState);

        var playerState = new PlayerStateEntity(playerParticipation, gameState, EPlayerGameState.MustWait);
        playerStateRepository.save(playerState);
        logger.info("test: {} -> {}", playerState, playerState.getPlayerRound());
    }

    private void createSecondGameState(PlayerParticipationEntity playerOne, PlayerParticipationEntity playerTwo,
                                       GameEntity game) {
        EPlayerGameState playerOneGameState;
        EPlayerGameState playerTwoGameState;
        CommandTimeEntity commandTime;
        if (playerOne.isFirstTurn()) {
            playerOneGameState = EPlayerGameState.MustAct;
            playerTwoGameState = EPlayerGameState.MustWait;
            commandTime = new CommandTimeEntity(playerOne, LocalDateTime.now());
        } else {
            playerOneGameState = EPlayerGameState.MustWait;
            playerTwoGameState = EPlayerGameState.MustAct;
            commandTime = new CommandTimeEntity(playerTwo, LocalDateTime.now());
        }
        commandTimeRepository.save(commandTime);

        GameStateEntity gameState = gameStateRepository.findFirstByGameIdOrderByNrDesc(game.getId())
                .orElseThrow(() -> new IllegalStateException("No game state exists yet."))
                .advanceGameState(null);
        gameStateRepository.save(gameState);

        var playerOneState = new PlayerStateEntity(playerOne, gameState, playerOneGameState);
        playerStateRepository.save(playerOneState);
        var playerTwoState = new PlayerStateEntity(playerTwo, gameState, playerTwoGameState);
        playerStateRepository.save(playerTwoState);
    }

    @NonNull
    private PlayerParticipationEntity getPlayerParticipation(UniqueGameIdentifier uniqueGameIdentifier,
                                                             UniquePlayerIdentifier uniquePlayerIdentifier) {
        if (uniqueGameIdentifier == null)
            throw new IllegalArgumentException("uniqueGameIdentifier is null");
        if (uniquePlayerIdentifier == null)
            throw new IllegalArgumentException("uniquePlayerIdentifier is null");

        String gameId = uniqueGameIdentifier.getUniqueGameID();
        gameRepository.findById(gameId)
                .orElseThrow(() -> new MatchNotFoundException(gameId));

        String playerId = uniquePlayerIdentifier.getUniquePlayerID();
        PlayerParticipationEntity playerParticipation = playerParticipationRepository.findById(playerId)
                .orElseThrow(() -> new PlayerUnknownException(gameId, playerId));

        if (!playerParticipation.getGame().getId().equals(gameId))
            throw new PlayerUnknownException(gameId, playerId);

        return playerParticipation;
    }

    @Transactional
    public GameState getGameState(UniqueGameIdentifier uniqueGameIdentifier, UniquePlayerIdentifier uniquePlayerIdentifier) {
        PlayerParticipationEntity playerParticipation = getPlayerParticipation(uniqueGameIdentifier, uniquePlayerIdentifier);
        queryTimeRepository.save(new QueryTimeEntity(playerParticipation, LocalDateTime.now()));

        GameStateEntity latestGameState =
                gameStateRepository.findFirstByGameIdOrderByNrDesc(uniqueGameIdentifier.getUniqueGameID())
                        .orElseThrow(() -> new IllegalStateException("No game state exists yet."));

        Set<PlayerState> playerStates = new HashSet<>();

        for (PlayerStateEntity dbPlayerState : latestGameState.getPlayerStates()) {
            PlayerParticipationEntity dbPlayerParticipation = dbPlayerState.getPlayerParticipation();
            String displayPlayerId =
                    dbPlayerParticipation.getDisplayPlayerIdFor(uniquePlayerIdentifier.getUniquePlayerID());

            PlayerRegistrationEntity playerRegistration = dbPlayerParticipation.getPlayerRegistration();

            var playerState = new PlayerState(
                    playerRegistration.getFirstName(),
                    playerRegistration.getLastName(),
                    playerRegistration.getUAccount(),
                    dbPlayerState.getPlayerGameState(),
                    new UniquePlayerIdentifier(displayPlayerId),
                    dbPlayerState.getPlayerRound().map(PlayerRoundEntity::hasCollectedTreasure).orElse(false)
            );
            playerStates.add(playerState);
        }

        return new GameState(playerStates, latestGameState.getId());
    }

    @Transactional
    public void submitHalfMap(UniqueGameIdentifier uniqueGameIdentifier, PlayerHalfMap playerHalfMap) {
        PlayerParticipationEntity playerParticipation = getPlayerParticipation(uniqueGameIdentifier, playerHalfMap);

        GameEntity game = playerParticipation.getGame();
        if (!game.isDebugMode()) {
            CommandTimeEntity commandTime = commandTimeRepository
                    .findFirstByPlayerParticipation_PlayerIdOrderByCommandAtDesc(playerParticipation.getPlayerId())
                    .orElseThrow(() -> new IllegalStateException("No last command time recorded."));

            long millisSinceLastCommand = ChronoUnit.MILLIS.between(commandTime.getCommandAt(), LocalDateTime.now());
            if (millisSinceLastCommand > MAXIMUM_ACTION_INTERVAL.toMillis()) {
                logger.warn("Player {} submitting half map too infrequently: {} ms",
                        playerParticipation.getPlayerRegistration().getUAccount(), millisSinceLastCommand);
                throw new TooSlowActionException();
            }
        }
        var commandTime = new CommandTimeEntity(playerParticipation, LocalDateTime.now());
        commandTimeRepository.save(commandTime);

        GameStateEntity latestGameState = gameStateRepository.findFirstByGameIdOrderByNrDesc(game.getId())
                .orElseThrow(() -> new IllegalStateException("No game state exists yet."));

        PlayerStateEntity currentPlayerState = latestGameState.getPlayerStates().stream()
                .filter(ps -> ps.getPlayerParticipation().getPlayerId().equals(playerParticipation.getPlayerId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Player not found in game state."));
        if (currentPlayerState.getPlayerGameState() != EPlayerGameState.MustAct)
            throw new PlayerTurnException();

        HalfMap halfMap = halfMapConverter.convertHalfMap(playerHalfMap);
        Notification notification = halfMapValidator.validate(halfMap);
        if (notification.hasErrors())
            throw notification.getErrors().getFirst();

        XYPair fortLocation = halfMap.potentialForts().stream()
                .skip(randomGenerator.nextInt(halfMap.potentialForts().size()))
                .findFirst()
                .orElseThrow();
        List<XYPair> potentialTreasures = halfMap.nodes().entrySet().stream()
                .filter(entry -> entry.getValue() == ETerrain.Grass)
                .filter(entry -> !fortLocation.equals(entry.getKey()))
                .map(Map.Entry::getKey)
                .toList();
        XYPair treasureLocation = potentialTreasures.get(randomGenerator.nextInt(potentialTreasures.size()));
        var playerFullMap =
                new PlayerFullMapEntity(playerParticipation, fortLocation.x(), fortLocation.y(), treasureLocation.x(), treasureLocation.y());
        playerFullMapRepository.save(playerFullMap);
    }
}