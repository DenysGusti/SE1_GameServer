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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.random.RandomGenerator;

@Service
public class GameService {
    private static final Logger logger = LoggerFactory.getLogger(GameService.class);
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int GAME_ID_LENGTH = 5;
    private static final int MAX_GAMES = 5;
    private static final int GAME_LIFETIME_MINUTES = 1;
    private static final int PLAYERS_PER_GAME = 2;
    private static final int MAX_GAMES_PER_PLAYER = 3;
    private static final Duration MINIMUM_POLLING_INTERVAL = Duration.ofMillis(300);
    private static final Duration MAXIMUM_ACTION_INTERVAL = Duration.ofSeconds(5);

    private final RandomGenerator randomGenerator;
    private final HalfMapValidator halfMapValidator;
    private final HalfMapConverter halfMapConverter;
    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;
    private final PlayerParticipationRepository playerParticipationRepository;
    private final GameStateRepository gameStateRepository;

    public GameService(RandomGenerator randomGenerator, HalfMapValidator halfMapValidator,
                       HalfMapConverter halfMapConverter, GameRepository gameRepository,
                       PlayerRepository playerRepository, PlayerParticipationRepository playerParticipationRepository,
                       GameStateRepository gameStateRepository) {
        if (randomGenerator == null)
            throw new IllegalArgumentException("randomGenerator is null");
        if (halfMapValidator == null)
            throw new IllegalArgumentException("halfMapValidator is null");
        if (halfMapConverter == null)
            throw new IllegalArgumentException("halfMapConverter is null");
        if (gameRepository == null)
            throw new IllegalArgumentException("gameRepository is null");
        if (playerRepository == null)
            throw new IllegalArgumentException("playerRepository is null");
        if (playerParticipationRepository == null)
            throw new IllegalArgumentException("playerParticipationRepository is null");
        if (gameStateRepository == null)
            throw new IllegalArgumentException("gameStateRepository is null");

        this.gameRepository = gameRepository;
        this.halfMapValidator = halfMapValidator;
        this.randomGenerator = randomGenerator;
        this.halfMapConverter = halfMapConverter;
        this.playerRepository = playerRepository;
        this.playerParticipationRepository = playerParticipationRepository;
        this.gameStateRepository = gameStateRepository;
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

        FullMapType fullMapType = randomGenerator.nextBoolean() ? FullMapType.Horizontal : FullMapType.Vertical;

        var game = new GameEntity(newGameId, debugMode, dummyCompetition, fullMapType);
        gameRepository.save(game);

        return new UniqueGameIdentifier(newGameId);
    }

    @Transactional
    public void removeOldGames() {
        LocalDateTime tenMinutesAgo = LocalDateTime.now().minusMinutes(GAME_LIFETIME_MINUTES);
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

        playerRepository.deleteOrphanedPlayers();
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
        if (!playerRepository.existsById(uAccount)) {
            logger.info("New player tried to join game with unknown uAccount: {}", uAccount);
//            throw new UnknownUAccountException(uAccount);
        }

        String gameId = uniqueGameIdentifier.getUniqueGameID();
        GameEntity game = gameRepository.findById(gameId)
                .orElseThrow(() -> new MatchNotFoundException(gameId));

        long currentPlayers = playerParticipationRepository.countByGameId(gameId);
        if (currentPlayers >= PLAYERS_PER_GAME)
            throw new PlayerRegisterRuleException();

        long activeGamesForPlayer = playerParticipationRepository.countByPlayer_uAccount(uAccount);
        if (activeGamesForPlayer >= MAX_GAMES_PER_PLAYER)
            throw new ManualGameCreationOveruseException();

        var player = playerRepository.findById(uAccount)
                .orElseGet(() -> new PlayerEntity(
                        uAccount,
                        playerRegistration.getStudentFirstName(),
                        playerRegistration.getStudentLastName()
                ));

        player.setFirstName(playerRegistration.getStudentFirstName());
        player.setLastName(playerRegistration.getStudentLastName());
        player = playerRepository.save(player);

        boolean isFirstTurn;
        PlayerParticipationEntity firstPlayerParticipation = null;
        if (currentPlayers == 0)
            isFirstTurn = randomGenerator.nextBoolean();
        else {
            firstPlayerParticipation = playerParticipationRepository.findByGameId(gameId).getFirst();
            isFirstTurn = !firstPlayerParticipation.isFirstTurn();
        }

        String fakePlayerId;
        do {
            fakePlayerId = java.util.UUID.randomUUID().toString();
        } while (playerParticipationRepository.existsById(fakePlayerId) ||
                playerParticipationRepository.existsByFakePlayerId(fakePlayerId));

        var playerParticipation = new PlayerParticipationEntity(fakePlayerId, player, game, isFirstTurn);
        playerParticipation = playerParticipationRepository.save(playerParticipation);

        GameStateEntity newGameState;
        if (currentPlayers == 0)
            newGameState = createFirstGameState(playerParticipation, game);
        else
            newGameState = createSecondGameState(firstPlayerParticipation, playerParticipation, game);
        gameStateRepository.save(newGameState);

        if (currentPlayers != 0) {
            if (firstPlayerParticipation.isFirstTurn())
                firstPlayerParticipation.updateLastCommandAt();
            else
                playerParticipation.updateLastCommandAt();
        }

        return new UniquePlayerIdentifier(playerParticipation.getPlayerId());
    }

    @NonNull
    private static GameStateEntity createFirstGameState(PlayerParticipationEntity firstPlayerParticipation,
                                                        GameEntity game) {
        var newGameState = new GameStateEntity(game, 0);
        var firstPlayerState =
                new PlayerStateEntity(firstPlayerParticipation, newGameState, EPlayerGameState.MustWait, false);

        newGameState.addPlayerState(firstPlayerState);
        return newGameState;
    }

    @NonNull
    private static GameStateEntity createSecondGameState(PlayerParticipationEntity firstPlayerParticipation,
                                                         PlayerParticipationEntity secondPlayerParticipation,
                                                         GameEntity game) {
        EPlayerGameState firstPlayerStateEnum = firstPlayerParticipation.isFirstTurn() ?
                EPlayerGameState.MustAct : EPlayerGameState.MustWait;
        EPlayerGameState secondPlayerStateEnum = secondPlayerParticipation.isFirstTurn() ?
                EPlayerGameState.MustAct : EPlayerGameState.MustWait;

        var newGameState = new GameStateEntity(game, 1);
        var firstPlayerState =
                new PlayerStateEntity(firstPlayerParticipation, newGameState, firstPlayerStateEnum, false);
        var secondPlayerState =
                new PlayerStateEntity(secondPlayerParticipation, newGameState, secondPlayerStateEnum, false);

        newGameState.addPlayerState(firstPlayerState);
        newGameState.addPlayerState(secondPlayerState);
        return newGameState;
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
        PlayerParticipationEntity requestingPlayer = getPlayerParticipation(uniqueGameIdentifier, uniquePlayerIdentifier);

        LocalDateTime now = LocalDateTime.now();
        requestingPlayer.getLastQueryAt().ifPresent(lastQuery -> {
            long millisSinceLastQuery = ChronoUnit.MILLIS.between(lastQuery, now);
            if (millisSinceLastQuery < MINIMUM_POLLING_INTERVAL.toMillis()) {
                logger.warn("Player {} polling too frequently: {} ms",
                        requestingPlayer.getPlayer().getUAccount(), millisSinceLastQuery);
                throw new TooFastPollingException();
            }
        });

        requestingPlayer.updateLastQueryAt();
        playerParticipationRepository.save(requestingPlayer);

        GameStateEntity latestGameState =
                gameStateRepository.findFirstByGameIdOrderByCurrentRoundDesc(uniqueGameIdentifier.getUniqueGameID())
                        .orElseThrow(() -> new IllegalStateException("No game state exists yet."));

        Set<PlayerState> playerStates = new HashSet<>();

        for (PlayerStateEntity dbPlayerState : latestGameState.getPlayerStates()) {
            PlayerParticipationEntity playerParticipation = dbPlayerState.getParticipation();
            var displayPlayerId = playerParticipation.getPlayerId().equals(uniquePlayerIdentifier.getUniquePlayerID()) ?
                    new UniquePlayerIdentifier(playerParticipation.getPlayerId()) :
                    new UniquePlayerIdentifier(playerParticipation.getFakePlayerId());

            PlayerEntity player = playerParticipation.getPlayer();

            var playerState = new PlayerState(
                    player.getFirstName(),
                    player.getLastName(),
                    player.getUAccount(),
                    dbPlayerState.getState(),
                    displayPlayerId,
                    dbPlayerState.hasFoundTreasure()
            );
            playerStates.add(playerState);
        }

        return new GameState(playerStates, latestGameState.getId());
    }

    @Transactional
    public void submitHalfMap(UniqueGameIdentifier uniqueGameIdentifier, PlayerHalfMap playerHalfMap) {
        PlayerParticipationEntity player = getPlayerParticipation(uniqueGameIdentifier, playerHalfMap);

        GameEntity game = player.getGame();
        if (!game.isDebugMode()) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime lastCommandAt = player.getLastCommandAt()
                    .orElseThrow(() -> new IllegalStateException("No last command time recorded."));

            long millisSinceLastCommand = ChronoUnit.MILLIS.between(lastCommandAt, now);
            if (millisSinceLastCommand > MAXIMUM_ACTION_INTERVAL.toMillis()) {
                logger.warn("Player {} submitting half map too infrequently: {} ms",
                        player.getPlayer().getUAccount(), millisSinceLastCommand);
                throw new TooSlowActionException();
            }
        }
        player.updateLastCommandAt();
        playerParticipationRepository.save(player);

        GameStateEntity latestGameState = gameStateRepository.findFirstByGameIdOrderByCurrentRoundDesc(game.getId())
                .orElseThrow(() -> new IllegalStateException("No game state exists yet."));

        PlayerStateEntity currentPlayerState = latestGameState.getPlayerStates().stream()
                .filter(ps -> ps.getParticipation().getPlayerId().equals(player.getPlayerId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Player not found in game state."));
        if (currentPlayerState.getState() != EPlayerGameState.MustAct)
            throw new PlayerTurnException();

        HalfMap halfMap = halfMapConverter.convertHalfMap(playerHalfMap);
        Notification notification = halfMapValidator.validate(halfMap);
        if (notification.hasErrors())
            throw notification.getErrors().getFirst();

        XYPair fortLocation = halfMap.potentialForts().stream()
                .skip(randomGenerator.nextInt(halfMap.potentialForts().size()))
                .findFirst()
                .orElseThrow();
        player.setFortLocation(fortLocation);

        List<XYPair> potentialTreasures = halfMap.nodes().entrySet().stream()
                .filter(entry -> entry.getValue() == ETerrain.Grass)
                .filter(entry -> !fortLocation.equals(entry.getKey()))
                .map(Map.Entry::getKey)
                .toList();
        XYPair treasureLocation = potentialTreasures.get(randomGenerator.nextInt(potentialTreasures.size()));
        player.setTreasureLocation(treasureLocation);

        halfMap.nodes().forEach((coordinate, terrain) -> {
            var node = new HalfMapNodeEntity(player, coordinate.x(), coordinate.y(), terrain,
                    fortLocation.equals(coordinate));
            player.addHalfMapNode(node);
        });

        playerParticipationRepository.save(player);

        var newGameState = new GameStateEntity(game, latestGameState.getCurrentRound() + 1);
    }
}