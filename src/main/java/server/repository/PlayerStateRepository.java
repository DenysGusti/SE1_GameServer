package server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import server.entity.PlayerStateEntity;

@Repository
public interface PlayerStateRepository extends JpaRepository<PlayerStateEntity, String> {
}
