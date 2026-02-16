package server.entities;

import jakarta.persistence.*;
import messagesbase.messagesfromclient.EMove;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "player_participation")
public class PlayerParticipationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, unique = true, updatable = false)
    private String playerId;

    @Column(nullable = false, unique = true, updatable = false)
    private String fakePlayerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "u_account", nullable = false)
    private PlayerEntity player;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private GameEntity game;

    private LocalDateTime lastQueryAt;

    private LocalDateTime lastCommandAt;

    private boolean firstTurn;

    private Integer fortX;
    private Integer fortY;
    private Integer treasureX;
    private Integer treasureY;

    @Enumerated(EnumType.STRING)
    private EMove pendingMoveDirection;

    private Integer pendingMoveCount;

    protected PlayerParticipationEntity() {
    }

    public PlayerParticipationEntity(String fakePlayerId, PlayerEntity player, GameEntity game, boolean firstTurn) {
        this.fakePlayerId = fakePlayerId;
        this.player = player;
        this.game = game;
        this.firstTurn = firstTurn;
    }

    public void updateLastQuery() {
        lastQueryAt = LocalDateTime.now();
    }

    public void updateLastCommand() {
        lastCommandAt = LocalDateTime.now();
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getFakePlayerId() {
        return fakePlayerId;
    }

    public PlayerEntity getPlayer() {
        return player;
    }

    public GameEntity getGame() {
        return game;
    }

    public LocalDateTime getLastQueryAt() {
        return lastQueryAt;
    }

    public LocalDateTime getLastCommandAt() {
        return lastCommandAt;
    }

    public boolean isFirstTurn() {
        return firstTurn;
    }

    public Integer getFortX() {
        return fortX;
    }

    public Integer getFortY() {
        return fortY;
    }

    public Integer getTreasureX() {
        return treasureX;
    }

    public Integer getTreasureY() {
        return treasureY;
    }

    public EMove getPendingMoveDirection() {
        return pendingMoveDirection;
    }

    public Integer getPendingMoveCount() {
        return pendingMoveCount;
    }

    public void setPlayer(PlayerEntity player) {
        this.player = player;
    }

    public void setGame(GameEntity game) {
        this.game = game;
    }

    public void setFirstTurn(boolean firstTurn) {
        this.firstTurn = firstTurn;
    }

    public void setFortLocation(Integer x, Integer y) {
        this.fortX = x;
        this.fortY = y;
    }

    public void setTreasureLocation(Integer x, Integer y) {
        this.treasureX = x;
        this.treasureY = y;
    }

    public void setPendingMove(EMove direction, Integer count) {
        this.pendingMoveDirection = direction;
        this.pendingMoveCount = count;
    }

    public void clearPendingMove() {
        this.pendingMoveDirection = null;
        this.pendingMoveCount = null;
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