package server.entity;

import jakarta.persistence.*;
import messagesbase.messagesfromclient.EMove;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "game_states", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"gameId", "nr"})
})
public class GameStateEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, unique = true, updatable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gameId", nullable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private GameEntity game;

    @Column(nullable = false, updatable = false)
    private int nr;

    @Enumerated(EnumType.STRING)
    @Column(updatable = false)
    private EMove transitionMove;

    @OneToMany(mappedBy = "gameState", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<PlayerStateEntity> playerStates = new ArrayList<>();

    protected GameStateEntity() {
    }

    public GameStateEntity(GameEntity game, int nr, EMove transitionMove) {
        if (game == null)
            throw new IllegalArgumentException("game is null");
        if (nr < 0)
            throw new IllegalArgumentException("nr is negative");

        this.game = game;
        this.nr = nr;
        this.transitionMove = transitionMove;
    }

    public GameStateEntity advanceGameState(EMove transitionMove) {
        return new GameStateEntity(game, nr + 1, transitionMove);
    }

    public String getId() {
        return id;
    }

    public int getNr() {
        return nr;
    }

    public EMove getTransitionMove() {
        return transitionMove;
    }

    public GameEntity getGame() {
        return game;
    }

    public List<PlayerStateEntity> getPlayerStates() {
        return List.copyOf(playerStates);
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