package server.entities;

import jakarta.persistence.*;
import messagesbase.messagesfromclient.EMove;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "game_states", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"game_id", "currentRound"})
})
public class GameStateEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, unique = true, updatable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false, updatable = false)
    private GameEntity game;

    @Column(nullable = false, updatable = false)
    private int currentRound;

    @Enumerated(EnumType.STRING)
    private EMove move;

    @OneToMany(mappedBy = "gameState", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<PlayerStateEntity> playerStates = new ArrayList<>();

    protected GameStateEntity() {
    }

    public GameStateEntity(GameEntity game, int currentRound, EMove move) {
        this.game = game;
        this.currentRound = currentRound;
        this.move = move;
    }

    public void addPlayerState(PlayerStateEntity playerState) {
        this.playerStates.add(playerState);
    }

    public String getId() {
        return id;
    }

    public GameEntity getGame() {
        return game;
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public EMove getMove() {
        return move;
    }

    public List<PlayerStateEntity> getPlayerStates() {
        return playerStates;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        var that = (GameStateEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}