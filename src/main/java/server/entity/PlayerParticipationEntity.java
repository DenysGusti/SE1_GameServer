package server.entity;

import jakarta.persistence.*;
import messagesbase.messagesfromclient.EMove;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import server.data.XYPair;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "u_account", nullable = false)
    private PlayerEntity player;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private GameEntity game;

    @OneToMany(mappedBy = "participation", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<HalfMapNodeEntity> halfMapNodes = new ArrayList<>();

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
        if (fakePlayerId == null)
            throw new IllegalArgumentException("fakePlayerId is null");
        if (player == null)
            throw new IllegalArgumentException("player is null");
        if (game == null)
            throw new IllegalArgumentException("game is null");

        this.fakePlayerId = fakePlayerId;
        this.player = player;
        this.game = game;
        this.firstTurn = firstTurn;
    }

    public void updateLastQueryAt() {
        lastQueryAt = LocalDateTime.now();
    }

    public void updateLastCommandAt() {
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

    public Optional<LocalDateTime> getLastQueryAt() {
        return Optional.ofNullable(lastQueryAt);
    }

    public Optional<LocalDateTime> getLastCommandAt() {
        return Optional.ofNullable(lastCommandAt);
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

    public void setFortLocation(XYPair fortLocation) {
        if (fortLocation == null)
            throw new IllegalArgumentException("fortLocation is null");

        fortX = fortLocation.x();
        fortY = fortLocation.y();
    }

    public void setTreasureLocation(XYPair treasureLocation) {
        if (treasureLocation == null)
            throw new IllegalArgumentException("treasureLocation is null");

        treasureX = treasureLocation.x();
        treasureY = treasureLocation.y();
    }

    public void addHalfMapNode(HalfMapNodeEntity node) {
        if (node == null)
            throw new IllegalArgumentException("node is null");

        halfMapNodes.add(node);
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