package server.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.Objects;

@Entity
@Table(name = "player_full_maps")
public class PlayerFullMapEntity {
    @Id
    @Column(nullable = false, unique = true, updatable = false)
    private String playerId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "playerId")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private PlayerParticipationEntity playerParticipation;

    @Column(nullable = false, updatable = false)
    private int fortX;

    @Column(nullable = false, updatable = false)
    private int fortY;

    @Column(nullable = false, updatable = false)
    private int treasureX;

    @Column(nullable = false, updatable = false)
    private int treasureY;

    protected PlayerFullMapEntity() {
    }

    public PlayerFullMapEntity(PlayerParticipationEntity playerParticipation, int fortX, int fortY, int treasureX, int treasureY) {
        if (playerParticipation == null)
            throw new IllegalArgumentException("playerParticipation is null");

        this.playerParticipation = playerParticipation;
        this.fortX = fortX;
        this.fortY = fortY;
        this.treasureX = treasureX;
        this.treasureY = treasureY;
    }

    public String getPlayerId() {
        return playerId;
    }

    public PlayerParticipationEntity getPlayerParticipation() {
        return playerParticipation;
    }

    public int getFortX() {
        return fortX;
    }

    public int getFortY() {
        return fortY;
    }

    public int getTreasureX() {
        return treasureX;
    }

    public int getTreasureY() {
        return treasureY;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        var that = (PlayerFullMapEntity) o;
        return Objects.equals(playerId, that.playerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerId);
    }
}
