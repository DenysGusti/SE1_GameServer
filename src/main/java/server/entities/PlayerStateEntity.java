package server.entities;

import jakarta.persistence.*;
import messagesbase.messagesfromserver.EPlayerGameState;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.Objects;

@Entity
@Table(name = "player_states", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"participation_id", "game_state_id"})
})
public class PlayerStateEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, unique = true, updatable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participation_id", nullable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private PlayerParticipationEntity participation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_state_id", nullable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private GameStateEntity gameState;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EPlayerGameState state;

    @Column(nullable = false)
    private boolean foundTreasure;

    protected PlayerStateEntity() {
    }

    public PlayerStateEntity(PlayerParticipationEntity participation, GameStateEntity gameState, EPlayerGameState state, boolean foundTreasure) {
        if (participation == null)
            throw new IllegalArgumentException("participation is null");
        if (gameState == null)
            throw new IllegalArgumentException("gameState is null");
        if (state == null)
            throw new IllegalArgumentException("state is null");

        this.participation = participation;
        this.gameState = gameState;
        this.state = state;
        this.foundTreasure = foundTreasure;
    }

    public PlayerParticipationEntity getParticipation() {
        return participation;
    }

    public GameStateEntity getGameState() {
        return gameState;
    }

    public EPlayerGameState getState() {
        return state;
    }

    public boolean hasFoundTreasure() {
        return foundTreasure;
    }

    public void setState(EPlayerGameState state) {
        this.state = state;
    }

    public void setFoundTreasure(boolean foundTreasure) {
        this.foundTreasure = foundTreasure;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        var that = (PlayerStateEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}