package server.entities;

import jakarta.persistence.*;
import messagesbase.messagesfromclient.ETerrain;

import java.util.Objects;

@Entity
@Table(name = "full_map_nodes")
@IdClass(FullMapNodeId.class)
public class FullMapNodeEntity {
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false, updatable = false)
    private GameEntity game;

    @Id
    @Column(nullable = false, updatable = false)
    private int x;

    @Id
    @Column(nullable = false, updatable = false)
    private int y;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private ETerrain terrain;

    protected FullMapNodeEntity() {
    }

    public FullMapNodeEntity(GameEntity game, int x, int y, ETerrain terrain) {
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
        return x == that.x && y == that.y && Objects.equals(game, that.game);
    }

    @Override
    public int hashCode() {
        return Objects.hash(game, x, y);
    }
}