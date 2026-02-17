package server.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import server.entities.PlayerParticipationEntity;

import java.util.List;

@Repository
public interface PlayerParticipationRepository extends JpaRepository<PlayerParticipationEntity, String> {
    long countByGameId(String gameId);

    long countByPlayer_uAccount(String uAccount);

    List<PlayerParticipationEntity> findByGameId(String gameId);
}