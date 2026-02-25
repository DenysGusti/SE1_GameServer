package server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import server.entity.CommandTimeEntity;

import java.util.Optional;

@Repository
public interface CommandTimeRepository extends JpaRepository<CommandTimeEntity, String> {
    Optional<CommandTimeEntity> findFirstByPlayerParticipation_PlayerIdOrderByCommandAtDesc(String playerId);
}
