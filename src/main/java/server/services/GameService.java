package server.services;

import messagesbase.UniqueGameIdentifier;
import messagesbase.UniquePlayerIdentifier;
import messagesbase.messagesfromclient.PlayerRegistration;
import messagesbase.messagesfromserver.EPlayerGameState;
import messagesbase.messagesfromserver.GameState;
import messagesbase.messagesfromserver.PlayerState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import server.entities.*;
import server.exceptions.*;
import server.repositories.GameRepository;
import server.repositories.GameStateRepository;
import server.repositories.PlayerParticipationRepository;
import server.repositories.PlayerRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
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
    private static final Duration MINIMUM_POLLING_INTERVAL = Duration.ofMillis(400);

    private final RandomGenerator randomGenerator;
    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;
    private final PlayerParticipationRepository playerParticipationRepository;
    private final GameStateRepository gameStateRepository;

    public GameService(RandomGenerator randomGenerator, GameRepository gameRepository,
                       PlayerRepository playerRepository, PlayerParticipationRepository playerParticipationRepository,
                       GameStateRepository gameStateRepository) {
        if (randomGenerator == null)
            throw new IllegalArgumentException("randomGenerator is null");
        if (gameRepository == null)
            throw new IllegalArgumentException("gameRepository is null");
        if (playerRepository == null)
            throw new IllegalArgumentException("playerRepository is null");
        if (playerParticipationRepository == null)
            throw new IllegalArgumentException("playerParticipationRepository is null");
        if (gameStateRepository == null)
            throw new IllegalArgumentException("gameStateRepository is null");

        this.gameRepository = gameRepository;
        this.randomGenerator = randomGenerator;
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
        if (currentPlayers == 0) {
            newGameState = new GameStateEntity(game, 0);
            var playerState = new PlayerStateEntity(playerParticipation, newGameState, EPlayerGameState.MustWait, false);
            newGameState.addPlayerState(playerState);
        } else
            newGameState = createSecondGameState(firstPlayerParticipation, playerParticipation, game);
        gameStateRepository.save(newGameState);

        return new UniquePlayerIdentifier(playerParticipation.getPlayerId());
    }

    @NonNull
    private static GameStateEntity createSecondGameState(PlayerParticipationEntity firstPlayerParticipation,
                                                PlayerParticipationEntity secondPlayerParticipation, GameEntity game) {
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

//    @Transactional
//    public GameState getGameState(UniqueGameIdentifier uniqueGameIdentifier, UniquePlayerIdentifier uniquePlayerIdentifier) {
//        String gameId = uniqueGameIdentifier.getUniqueGameID();
//        GameEntity game = gameRepository.findById(gameId)
//                .orElseThrow(() -> new MatchNotFoundException(gameId));
//
//        String playerId = uniquePlayerIdentifier.getUniquePlayerID();
//        PlayerParticipationEntity requestingPlayer = playerParticipationRepository.findById(playerId)
//                .orElseThrow(() -> new PlayerUnknownException(gameId, playerId));
//
//        if (!requestingPlayer.getGame().getId().equals(gameId))
//            throw new PlayerUnknownException(gameId, playerId);
//
//        LocalDateTime now = LocalDateTime.now();
//        requestingPlayer.getLastQueryAt().ifPresent(lastQuery -> {
//            long millisSinceLastQuery = ChronoUnit.MILLIS.between(lastQuery, now);
//            if (millisSinceLastQuery < MINIMUM_POLLING_INTERVAL.toMillis())
//                throw new TooFastPollingException();
//        });
//
//        requestingPlayer.updateLastQuery();
//        playerParticipationRepository.save(requestingPlayer);
//
//        List<PlayerParticipationEntity> allParticipations = playerParticipationRepository.findByGameId(gameId);
//        Set<PlayerState> playerStates = new HashSet<>();
//
//        for (PlayerParticipationEntity p : allParticipations) {
//            // Placeholder state logic: assigned based on the 'firstTurn' boolean
//            EPlayerGameState state = p.isFirstTurn() ? EPlayerGameState.MustAct : EPlayerGameState.MustWait;
//
//            PlayerState ps = new PlayerState(
//                    p.getPlayer().getFirstName(),
//                    p.getPlayer().getLastName(),
//                    p.getPlayer().getUAccount(),
//                    state,
//                    new UniquePlayerIdentifier(p.getPlayerId()),
//                    p.getTreasureX() != null // true if they found the treasure, false otherwise
//            );
//            playerStates.add(ps);
//        }
//
//        String gameStateId = java.util.UUID.randomUUID().toString();
//
//        return new GameState(playerStates, new UniqueGameStateIdentifier(gameStateId));
//    }
}