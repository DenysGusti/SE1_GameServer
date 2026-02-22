package server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import server.entity.GameEntity;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface GameRepository extends JpaRepository<GameEntity, String> {

    void deleteByCreatedAtBefore(LocalDateTime cutoff);

    List<GameEntity> findAllByOrderByCreatedAtAsc();
}