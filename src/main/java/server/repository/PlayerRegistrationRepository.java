package server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import server.entity.PlayerRegistrationEntity;

@Repository
public interface PlayerRegistrationRepository extends JpaRepository<PlayerRegistrationEntity, String> {
    @Modifying
    @Query("""
            DELETE FROM PlayerRegistrationEntity playerRegistration WHERE NOT EXISTS (
                SELECT 1 FROM PlayerParticipationEntity playerParticipation WHERE
                            playerParticipation.playerRegistration = playerRegistration)
           """)
    void deleteOrphanedPlayerRegistrations();
}