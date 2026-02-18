package server.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import server.entities.GameStateEntity;

@Repository
public interface GameStateRepository extends JpaRepository<GameStateEntity, String> {
}
