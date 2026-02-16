package server.services;

import messagesbase.UniqueGameIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import server.entities.GameEntity;
import server.repositories.GameRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.random.RandomGenerator;

@Service
public class GameService {
    private static final Logger logger = LoggerFactory.getLogger(GameService.class);
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int GAME_ID_LENGTH = 5;
    private static final int MAX_GAMES = 99;
    private static final int GAME_LIFETIME_MINUTES = 1;

    private final GameRepository gameRepository;
    private final RandomGenerator randomGenerator;

    public GameService(GameRepository gameRepository, RandomGenerator randomGenerator) {
        if (gameRepository == null)
            throw new IllegalArgumentException("gameRepository is null");
        if (randomGenerator == null)
            throw new IllegalArgumentException("randomGenerator is null");

        this.gameRepository = gameRepository;
        this.randomGenerator = randomGenerator;
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

        var game = new GameEntity(newGameId, debugMode, dummyCompetition);
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
    }
}