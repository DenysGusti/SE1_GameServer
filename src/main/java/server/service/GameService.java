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
@Transactional(noRollbackFor = {GenericServerException.class})
public class GameService {
    private static final Logger logger = LoggerFactory.getLogger(GameService.class);

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int GAME_ID_LENGTH = 5;

    private static final int MAX_GAMES = 5;
    private static final int PLAYERS_PER_GAME = 2;
    private static final int MAX_GAMES_PER_PLAYER = 100;
    private static final int HALF_MAP_NODES = 50;
    private static final int FULL_MAP_NODES = 100;

    private static final XYPair HALF_MAP_SIZE = new XYPair(10, 5);

    private static final Duration GAME_LIFETIME = Duration.ofMinutes(10);
    //    private static final Duration MINIMUM_POLLING_INTERVAL = Duration.ofMillis(300);
    private static final Duration MAXIMUM_ACTION_INTERVAL = Duration.ofMillis(5500);

    private static final int REVEAL_GAME_STATE_NR = 17;
    private static final int MAX_GAME_STATE_NR = 319;

    private static final Map<ETerrain, Integer> terrainMovementCost = Map.of(
            ETerrain.Grass, 1,
            ETerrain.Mountain, 2
    );

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
                       PlayerStateRepository playerStateRepository
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
    }

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
            fakePlayerId = UUID.randomUUID().toString();
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
        var gameState = new GameStateEntity(game, 0);
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
                .advanceGameState();
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

    public GameState getGameState(UniqueGameIdentifier uniqueGameIdentifier, UniquePlayerIdentifier uniquePlayerIdentifier) {
        PlayerParticipationEntity playerParticipation = getPlayerParticipation(uniqueGameIdentifier, uniquePlayerIdentifier);
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

        Optional<PlayerStateEntity> myPlayerState = latestGameState.getPlayerStates().stream()
                .filter(ps -> ps.getPlayerParticipation().getPlayerId().equals(playerParticipation.getPlayerId()))
                .findFirst();
        Optional<PlayerStateEntity> enemyPlayerState = latestGameState.getPlayerStates().stream()
                .filter(ps -> !ps.getPlayerParticipation().getPlayerId().equals(playerParticipation.getPlayerId()))
                .findFirst();

        Optional<PlayerRoundEntity> myPlayerPlayerRound = myPlayerState.flatMap(PlayerStateEntity::getPlayerRound);
        Optional<PlayerFullMapEntity> myPlayerFullMap = myPlayerState.flatMap(ps -> ps.getPlayerParticipation().getPlayerFullMap());

        Optional<PlayerRoundEntity> enemyPlayerPlayerRound = enemyPlayerState.flatMap(PlayerStateEntity::getPlayerRound);
        Optional<PlayerFullMapEntity> enemyPlayerFullMap = enemyPlayerState.flatMap(ps -> ps.getPlayerParticipation().getPlayerFullMap());

        boolean hasMe = myPlayerPlayerRound.isPresent() && myPlayerFullMap.isPresent();
        boolean hasEnemy = enemyPlayerPlayerRound.isPresent() && enemyPlayerFullMap.isPresent();

        if (!hasMe && !hasEnemy)
            return new GameState(playerStates, latestGameState.getId());

        XYPair my;
        XYPair myFort;
        XYPair myTreasure;
        int fowRadius;
        if (hasMe) {
            my = new XYPair(myPlayerPlayerRound.orElseThrow().getPlayerX(), myPlayerPlayerRound.orElseThrow().getPlayerY());
            myFort = new XYPair(myPlayerFullMap.orElseThrow().getFortX(), myPlayerFullMap.orElseThrow().getFortY());
            myTreasure = new XYPair(myPlayerFullMap.orElseThrow().getTreasureX(), myPlayerFullMap.orElseThrow().getTreasureY());

            boolean amIOnMountain = latestGameState.getGame().getFullMapNodes().stream()
                    .anyMatch(n -> n.getX() == my.x() && n.getY() == my.y() && n.getTerrain() == ETerrain.Mountain);
            if (amIOnMountain)
                fowRadius = 1;
            else
                fowRadius = 0;
        } else {
            fowRadius = 0;
            my = null;
            myFort = null;
            myTreasure = null;
        }

        XYPair enemy;
        XYPair enemyFort;
        if (hasEnemy) {
            if (latestGameState.getNr() >= REVEAL_GAME_STATE_NR)
                enemy = new XYPair(enemyPlayerPlayerRound.orElseThrow().getPlayerX(), enemyPlayerPlayerRound.orElseThrow().getPlayerY());
            else {
                List<FullMapNodeEntity> validFakeTiles = latestGameState.getGame().getFullMapNodes().stream()
                        .filter(n -> n.getTerrain() != ETerrain.Water)
                        .toList();
                int randomIndex = new Random(latestGameState.getNr()).nextInt(validFakeTiles.size());
                FullMapNodeEntity fakeTile = validFakeTiles.get(randomIndex);
                enemy = new XYPair(fakeTile.getX(), fakeTile.getY());
            }
            enemyFort = new XYPair(enemyPlayerFullMap.orElseThrow().getFortX(), enemyPlayerFullMap.orElseThrow().getFortY());
        } else {
            enemy = null;
            enemyFort = null;
        }

        List<FullMapNode> fullMapNodes = latestGameState.getGame().getFullMapNodes().stream()
                .map(fullMapNode -> {
                    var node = new XYPair(fullMapNode.getX(), fullMapNode.getY());

                    EPlayerPositionState playerPositionState = EPlayerPositionState.NoPlayerPresent;
                    ETreasureState treasureState = ETreasureState.NoOrUnknownTreasureState;
                    EFortState fortState = EFortState.NoOrUnknownFortState;

                    boolean isMeHere = hasMe && node.equals(my);
                    boolean isEnemyHere = hasEnemy && node.equals(enemy);

                    if (isMeHere && isEnemyHere)
                        playerPositionState = EPlayerPositionState.BothPlayerPosition;
                    else if (isMeHere)
                        playerPositionState = EPlayerPositionState.MyPlayerPosition;
                    else if (isEnemyHere)
                        playerPositionState = EPlayerPositionState.EnemyPlayerPosition;

                    if (hasMe && hasEnemy) {
                        if (Math.max(Math.abs(my.x() - node.x()), Math.abs(my.y() - node.y())) <= fowRadius) {
                            if (node.equals(enemyFort))
                                fortState = EFortState.EnemyFortPresent;
                            else if (node.equals(myTreasure))
                                treasureState = ETreasureState.MyTreasureIsPresent;
                        }
                    }

                    if (hasMe && node.equals(myFort))
                        fortState = EFortState.MyFortPresent;

                    return new FullMapNode(fullMapNode.getTerrain(), playerPositionState, treasureState, fortState, node.x(), node.y());
                })
                .toList();

        var fullMap = new FullMap(fullMapNodes);

        return new GameState(fullMap, playerStates, latestGameState.getId());
    }

    private void endGame(PlayerParticipationEntity lostPlayerParticipation, EMove move) {
        GameEntity game = lostPlayerParticipation.getGame();
        GameStateEntity latestGameState = gameStateRepository.findFirstByGameIdOrderByNrDesc(game.getId())
                .orElseThrow(() -> new IllegalStateException("No game state exists yet."));

        GameStateEntity newGameState = latestGameState.advanceGameState(move);
        gameStateRepository.save(newGameState);

        for (PlayerStateEntity playerState : latestGameState.getPlayerStates()) {
            boolean lost = playerState.getPlayerParticipation().equals(lostPlayerParticipation);
            PlayerStateEntity newPlayerState = playerState.endPlayerState(newGameState, lost);
            playerStateRepository.save(newPlayerState);
        }
    }

    private void checkCommandTime(PlayerParticipationEntity playerParticipation, EMove move) {
        CommandTimeEntity commandTime = commandTimeRepository
                .findFirstByPlayerParticipation_PlayerIdOrderByCommandAtDesc(playerParticipation.getPlayerId())
                .orElseThrow(() -> new IllegalStateException("No last command time recorded."));

        LocalDateTime now = LocalDateTime.now();

        long millisSinceLastCommand = ChronoUnit.MILLIS.between(commandTime.getCommandAt(), now);
        if (millisSinceLastCommand > MAXIMUM_ACTION_INTERVAL.toMillis()) {
            logger.warn("Player {} submitting action too infrequently: {} ms",
                    playerParticipation.getPlayerRegistration().getUAccount(), millisSinceLastCommand);

            PlayerStateEntity winningPlayerState = endGame(playerParticipation);
            winningPlayerState.getPlayerRound().ifPresent(playerRound -> {
                PlayerRoundEntity newPlayerRound = playerRound.advancePlayerRound(winningPlayerState);
                playerRoundRepository.save(newPlayerRound);
            });
            throw new TooSlowActionException();
        }

        commandTimeRepository.save(new CommandTimeEntity(playerParticipation, now));
    }

    public void submitHalfMap(UniqueGameIdentifier uniqueGameIdentifier, PlayerHalfMap playerHalfMap) {
        PlayerParticipationEntity playerParticipation = getPlayerParticipation(uniqueGameIdentifier, playerHalfMap);

        GameEntity game = playerParticipation.getGame();
        GameStateEntity latestGameState = gameStateRepository.findFirstByGameIdOrderByNrDesc(game.getId())
                .orElseThrow(() -> new IllegalStateException("No game state exists yet."));

        if (!game.isDebugMode())
            checkCommandTime(playerParticipation);

        PlayerStateEntity currentPlayerState = latestGameState.getPlayerStates().stream()
                .filter(ps -> ps.getPlayerParticipation().getPlayerId().equals(playerParticipation.getPlayerId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Player not found in game state."));
        if (currentPlayerState.getPlayerGameState() != EPlayerGameState.MustAct) {
            endGame(playerParticipation, null);
            throw new PlayerTurnException();
        }

        List<FullMapNodeEntity> fullMapNodes = game.getFullMapNodes();
        if (fullMapNodes.size() == FULL_MAP_NODES)
            throw new DuplicateHalfMapSubmissionException();

        if (!fullMapNodes.isEmpty() && fullMapNodes.size() != HALF_MAP_NODES)
            throw new IllegalStateException("Unexpected number of full map nodes.");

        HalfMap halfMap = halfMapConverter.convertHalfMap(playerHalfMap);
        Notification notification = halfMapValidator.validate(halfMap);
        if (notification.hasErrors()) {
            PlayerStateEntity winningPlayerState = endGame(playerParticipation);
            winningPlayerState.getPlayerRound().ifPresent(playerRound -> {
                PlayerRoundEntity newPlayerRound = playerRound.advancePlayerRound(winningPlayerState);
                playerRoundRepository.save(newPlayerRound);
            });
            throw notification.getErrors().getFirst();
        }

        boolean isTopOrLeftSide = playerParticipation.isFirstTurn() == game.hasFirstPlayerTopOrLeftSide();
        XYPair offset;
        if (isTopOrLeftSide)
            offset = new XYPair(0, 0);
        else if (game.hasHorizontalFullMap())
            offset = new XYPair(HALF_MAP_SIZE.x(), 0);
        else
            offset = new XYPair(0, HALF_MAP_SIZE.y());

        List<FullMapNodeEntity> newFullMapNodes = halfMap.nodes().entrySet().stream()
                .map(entry -> {
                    XYPair halfMapCoordinate = entry.getKey();
                    var fullMapCoordinate = new XYPair(halfMapCoordinate.x() + offset.x(), halfMapCoordinate.y() + offset.y());
                    return new FullMapNodeEntity(game, fullMapCoordinate.x(), fullMapCoordinate.y(), entry.getValue());
                })
                .toList();
        fullMapNodeRepository.saveAll(newFullMapNodes);

        XYPair halfMapFortLocation = halfMap.potentialForts().stream()
                .skip(randomGenerator.nextInt(halfMap.potentialForts().size()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No forts found in half map."));
        List<XYPair> potentialTreasures = halfMap.nodes().entrySet().stream()
                .filter(entry -> entry.getValue() == ETerrain.Grass)
                .filter(entry -> !halfMapFortLocation.equals(entry.getKey()))
                .map(Map.Entry::getKey)
                .toList();
        XYPair halfMapTreasureLocation = potentialTreasures.get(randomGenerator.nextInt(potentialTreasures.size()));

        var fullMapFortLocation = new XYPair(halfMapFortLocation.x() + offset.x(), halfMapFortLocation.y() + offset.y());
        var fullMapTreasureLocation = new XYPair(halfMapTreasureLocation.x() + offset.x(), halfMapTreasureLocation.y() + offset.y());

        var playerFullMap = new PlayerFullMapEntity(playerParticipation, fullMapFortLocation.x(), fullMapFortLocation.y(),
                fullMapTreasureLocation.x(), fullMapTreasureLocation.y());
        playerFullMapRepository.save(playerFullMap);

        GameStateEntity newGameState = latestGameState.advanceGameState(null);
        gameStateRepository.save(newGameState);

        PlayerStateEntity newMyPlayerState = null;

        for (PlayerStateEntity playerState : latestGameState.getPlayerStates()) {
            PlayerStateEntity newPlayerState = playerState.advancePlayerState(newGameState);
            playerStateRepository.save(newPlayerState);

            if (playerState.getPlayerParticipation().getPlayerId().equals(playerParticipation.getPlayerId()))
                newMyPlayerState = newPlayerState;

            playerState.getPlayerRound().ifPresent(playerRound -> {
                PlayerRoundEntity newPlayerRound = playerRound.advancePlayerRound(newPlayerState);
                playerRoundRepository.save(newPlayerRound);
            });

            if (newPlayerState.getPlayerGameState() == EPlayerGameState.MustAct)
                commandTimeRepository.save(new CommandTimeEntity(newPlayerState.getPlayerParticipation(), LocalDateTime.now()));
        }

        var newPlayerRound = new PlayerRoundEntity(newMyPlayerState, playerFullMap.getFortX(), playerFullMap.getFortY(), false);
        playerRoundRepository.save(newPlayerRound);
    }
}