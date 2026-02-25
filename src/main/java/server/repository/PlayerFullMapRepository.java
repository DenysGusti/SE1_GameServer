package server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import server.entity.PlayerFullMapEntity;

@Repository
public interface PlayerFullMapRepository extends JpaRepository<PlayerFullMapEntity, String> {
}
