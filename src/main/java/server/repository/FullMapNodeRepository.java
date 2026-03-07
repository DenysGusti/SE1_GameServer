package server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import server.entity.FullMapNodeEntity;

import java.util.Optional;

@Repository
public interface FullMapNodeRepository extends JpaRepository<FullMapNodeEntity, Long> {
    Optional<FullMapNodeEntity> findFirstByGameIdAndXAndY(String gameId, int x, int y);
}
