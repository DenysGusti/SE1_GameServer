package server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import server.entity.PlayerEntity;

@Repository
public interface PlayerRepository extends JpaRepository<PlayerEntity, String> {
    @Modifying
    @Query("DELETE FROM PlayerEntity p WHERE NOT EXISTS (SELECT 1 FROM PlayerParticipationEntity pp WHERE pp.player = p)")
    void deleteOrphanedPlayers();
}