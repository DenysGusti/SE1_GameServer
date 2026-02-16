package server.entities;

import jakarta.persistence.*;
import messagesbase.messagesfromserver.EPlayerGameState;

import java.util.Objects;

@Entity
@Table(name = "player_states")
@IdClass(PlayerStateId.class)
public class PlayerStateEntity {
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participation_id", nullable = false, updatable = false)
    private PlayerParticipationEntity participation;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_state_id", nullable = false, updatable = false)
    private GameStateEntity gameState;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EPlayerGameState state;

    @Column(nullable = false)
    private boolean foundTreasure;

    protected PlayerStateEntity() {
    }

    public PlayerStateEntity(PlayerParticipationEntity participation, GameStateEntity gameState, EPlayerGameState state, boolean foundTreasure) {
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
        return Objects.equals(participation, that.participation) && Objects.equals(gameState, that.gameState);
    }

    @Override
    public int hashCode() {
        return Objects.hash(participation, gameState);
    }
}