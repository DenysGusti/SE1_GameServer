package server.entities;

import jakarta.persistence.*;
import messagesbase.messagesfromclient.ETerrain;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.Objects;

@Entity
@Table(name = "half_map_nodes", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"participation_id", "x", "y"})
})
public class HalfMapNodeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, unique = true, updatable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participation_id", nullable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private PlayerParticipationEntity participation;

    @Column(nullable = false, updatable = false)
    private int x;

    @Column(nullable = false, updatable = false)
    private int y;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private ETerrain terrain;

    @Column(nullable = false, updatable = false)
    private boolean fortPresent;

    protected HalfMapNodeEntity() {
    }

    public HalfMapNodeEntity(PlayerParticipationEntity participation, int x, int y, ETerrain terrain, boolean fortPresent) {
        if (participation == null)
            throw new IllegalArgumentException("participation is null");
        if (x < 0)
            throw new IllegalArgumentException("x is negative");
        if (y < 0)
            throw new IllegalArgumentException("y is negative");
        if (terrain == null)
            throw new IllegalArgumentException("terrain is null");

        this.participation = participation;
        this.x = x;
        this.y = y;
        this.terrain = terrain;
        this.fortPresent = fortPresent;
    }

    public PlayerParticipationEntity getParticipation() {
        return participation;
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

    public boolean isFortPresent() {
        return fortPresent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        var that = (HalfMapNodeEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}