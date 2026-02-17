package server.services;

import messagesbase.UniqueGameIdentifier;
import messagesbase.UniquePlayerIdentifier;
import messagesbase.messagesfromclient.PlayerRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import server.entities.FullMapType;
import server.entities.GameEntity;
import server.entities.PlayerEntity;
import server.entities.PlayerParticipationEntity;
import server.exceptions.ManualGameCreationOveruseException;
import server.exceptions.MatchNotFoundException;
import server.exceptions.PlayerRegisterRuleException;
import server.exceptions.UnknownUAccountException;
import server.repositories.GameRepository;
import server.repositories.PlayerParticipationRepository;
import server.repositories.PlayerRepository;

import java.time.LocalDateTime;
import java.util.List;
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

    private final RandomGenerator randomGenerator;
    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;
    private final PlayerParticipationRepository playerParticipationRepository;

    public GameService(RandomGenerator randomGenerator, GameRepository gameRepository, PlayerRepository playerRepository,
                       PlayerParticipationRepository playerParticipationRepository) {
        if (randomGenerator == null)
            throw new IllegalArgumentException("randomGenerator is null");
        if (gameRepository == null)
            throw new IllegalArgumentException("gameRepository is null");
        if (playerRepository == null)
            throw new IllegalArgumentException("playerRepository is null");
        if (playerParticipationRepository == null)
            throw new IllegalArgumentException("playerParticipationRepository is null");

        this.gameRepository = gameRepository;
        this.randomGenerator = randomGenerator;
        this.playerRepository = playerRepository;
        this.playerParticipationRepository = playerParticipationRepository;
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
    public UniquePlayerIdentifier registerPlayer(UniqueGameIdentifier uniqueGameIdentifier, PlayerRegistration playerRegistration) {
        String uAccount = playerRegistration.getStudentUAccount();
        // optional to restrict players
//        if (!playerRepository.existsById(uAccount))
//            throw new UnknownUAccountException(uAccount);

        String gameId = uniqueGameIdentifier.getUniqueGameID();
        GameEntity game = gameRepository.findById(gameId)
                .orElseThrow(() -> new MatchNotFoundException(gameId));

        long currentPlayers = playerParticipationRepository.countByGameId(gameId);
        if (currentPlayers >= PLAYERS_PER_GAME)
            throw new PlayerRegisterRuleException();

        long activeGamesForStudent = playerParticipationRepository.countByPlayer_uAccount(uAccount);
        if (activeGamesForStudent >= MAX_GAMES_PER_PLAYER)
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
        if (currentPlayers == 0)
            isFirstTurn = randomGenerator.nextBoolean();
        else {
            PlayerParticipationEntity firstPlayer = playerParticipationRepository.findByGameId(gameId).getFirst();
            isFirstTurn = !firstPlayer.isFirstTurn();
        }
        String fakePlayerId = java.util.UUID.randomUUID().toString();
        var playerParticipation = new PlayerParticipationEntity(fakePlayerId, player, game, isFirstTurn);
        playerParticipation = playerParticipationRepository.save(playerParticipation);

        return new UniquePlayerIdentifier(playerParticipation.getPlayerId());
    }
}