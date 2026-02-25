package server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import server.entity.PlayerRoundEntity;

@Repository
public interface PlayerRoundRepository extends JpaRepository<PlayerRoundEntity, String> {
}
