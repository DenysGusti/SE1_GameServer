package server.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.Objects;

@Entity
@Table(name = "player_rounds")
public class PlayerRoundEntity {
    @Id
    @Column(nullable = false, unique = true, updatable = false)
    private String playerStateId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "playerStateId")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private PlayerStateEntity playerState;

    @Column(nullable = false, updatable = false)
    private int playerX;

    @Column(nullable = false, updatable = false)
    private int playerY;

    @Column(nullable = false, updatable = false)
    private boolean collectedTreasure;

    protected PlayerRoundEntity() {
    }

    public PlayerRoundEntity(PlayerStateEntity playerState, int playerX, int playerY, boolean collectedTreasure) {
        if (playerState == null)
            throw new IllegalArgumentException("playerState is null");
        if (playerX < 0)
            throw new IllegalArgumentException("playerX is negative");
        if (playerY < 0)
            throw new IllegalArgumentException("playerY is negative");

        this.playerState = playerState;
        this.playerX = playerX;
        this.playerY = playerY;
        this.collectedTreasure = collectedTreasure;
    }

    public PlayerRoundEntity advancePlayerRound(PlayerStateEntity playerState) {
        if (playerState == null)
            throw new IllegalArgumentException("playerState is null");

        return new PlayerRoundEntity(playerState, playerX, playerY, collectedTreasure);
    }

    public String getPlayerStateId() {
        return playerStateId;
    }

    public int getPlayerX() {
        return playerX;
    }

    public int getPlayerY() {
        return playerY;
    }

    public boolean hasCollectedTreasure() {
        return collectedTreasure;
    }

    public PlayerStateEntity getPlayerState() {
        return playerState;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        var that = (PlayerRoundEntity) o;
        return playerStateId.equals(that.playerStateId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerStateId);
    }
}
