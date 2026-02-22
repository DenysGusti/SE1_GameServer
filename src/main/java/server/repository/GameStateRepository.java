package server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import server.entity.GameStateEntity;

import java.util.Optional;

@Repository
public interface GameStateRepository extends JpaRepository<GameStateEntity, String> {

    Optional<GameStateEntity> findFirstByGameIdOrderByCurrentRoundDesc(String gameId);
}
