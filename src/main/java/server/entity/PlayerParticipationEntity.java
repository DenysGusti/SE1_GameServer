package server.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Entity
@Table(name = "player_participation")
public class PlayerParticipationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, unique = true, updatable = false)
    private String playerId;

    @Column(nullable = false, unique = true, updatable = false)
    private String fakePlayerId;

    @Column(nullable = false, updatable = false)
    private boolean firstTurn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uAccount", nullable = false)
    private PlayerRegistrationEntity playerRegistration;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gameId", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private GameEntity game;

    @OneToOne(mappedBy = "playerParticipation", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private PlayerFullMapEntity playerFullMap;

    @OneToMany(mappedBy = "playerParticipation", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommandTimeEntity> commandTimes;

    protected PlayerParticipationEntity() {
    }

    public PlayerParticipationEntity(String fakePlayerId, PlayerRegistrationEntity playerRegistration, GameEntity game, boolean firstTurn) {
        if (fakePlayerId == null)
            throw new IllegalArgumentException("fakePlayerId is null");
        if (playerRegistration == null)
            throw new IllegalArgumentException("playerRegistration is null");
        if (game == null)
            throw new IllegalArgumentException("game is null");

        this.fakePlayerId = fakePlayerId;
        this.playerRegistration = playerRegistration;
        this.game = game;
        this.firstTurn = firstTurn;
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getFakePlayerId() {
        return fakePlayerId;
    }

    public String getDisplayPlayerIdFor(String userPlayerId) {
        if (userPlayerId == null)
            throw new IllegalArgumentException("userPlayerId is null");

        return Objects.equals(userPlayerId, playerId) ? playerId : fakePlayerId;
    }

    public boolean isFirstTurn() {
        return firstTurn;
    }

    public PlayerRegistrationEntity getPlayerRegistration() {
        return playerRegistration;
    }

    public GameEntity getGame() {
        return game;
    }

    public Optional<PlayerFullMapEntity> getPlayerFullMap() {
        return Optional.ofNullable(playerFullMap);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        var that = (PlayerParticipationEntity) o;
        return Objects.equals(playerId, that.playerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerId);
    }
}