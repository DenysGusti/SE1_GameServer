package server.entity;

import jakarta.persistence.*;
import messagesbase.messagesfromserver.EPlayerGameState;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.Objects;
import java.util.Optional;

@Entity
@Table(name = "player_states", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"playerParticipationId", "gameStateId"})
})
public class PlayerStateEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, unique = true, updatable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "playerParticipationId", nullable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private PlayerParticipationEntity playerParticipation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gameStateId", nullable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private GameStateEntity gameState;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private EPlayerGameState playerGameState;

    @OneToOne(mappedBy = "playerState", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private PlayerRoundEntity playerRound;

    protected PlayerStateEntity() {
    }

    public PlayerStateEntity(PlayerParticipationEntity playerParticipation, GameStateEntity gameState, EPlayerGameState playerGameState) {
        if (playerParticipation == null)
            throw new IllegalArgumentException("playerParticipation is null");
        if (gameState == null)
            throw new IllegalArgumentException("gameState is null");
        if (playerGameState == null)
            throw new IllegalArgumentException("playerGameState is null");

        this.playerParticipation = playerParticipation;
        this.gameState = gameState;
        this.playerGameState = playerGameState;
    }

    public PlayerStateEntity advancePlayerState(GameStateEntity gameState) {
        return switch (playerGameState) {
            case MustAct -> new PlayerStateEntity(playerParticipation, gameState, EPlayerGameState.MustWait);
            case MustWait -> new PlayerStateEntity(playerParticipation, gameState, EPlayerGameState.MustAct);
            default -> throw new IllegalStateException("Invalid player game state: " + playerGameState);
        };
    }

    public EPlayerGameState getPlayerGameState() {
        return playerGameState;
    }

    public String getId() {
        return id;
    }

    public GameStateEntity getGameState() {
        return gameState;
    }

    public PlayerParticipationEntity getPlayerParticipation() {
        return playerParticipation;
    }

    public Optional<PlayerRoundEntity> getPlayerRound() {
        return Optional.ofNullable(playerRound);
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