package server.entities;

import jakarta.persistence.*;
import messagesbase.messagesfromclient.ETerrain;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.Objects;

@Entity
@Table(name = "full_map_nodes", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"game_id", "x", "y"})
})
public class FullMapNodeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, unique = true, updatable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private GameEntity game;

    @Column(nullable = false, updatable = false)
    private int x;

    @Column(nullable = false, updatable = false)
    private int y;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private ETerrain terrain;

    protected FullMapNodeEntity() {
    }

    public FullMapNodeEntity(GameEntity game, int x, int y, ETerrain terrain) {
        if (game == null)
            throw new IllegalArgumentException("game is null");
        if (x < 0)
            throw new IllegalArgumentException("x is negative");
        if (y < 0)
            throw new IllegalArgumentException("y is negative");
        if (terrain == null)
            throw new IllegalArgumentException("terrain is null");

        this.game = game;
        this.x = x;
        this.y = y;
        this.terrain = terrain;
    }

    public GameEntity getGame() {
        return game;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public ETerrain getTerrain() {
        return terrain;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        var that = (FullMapNodeEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}