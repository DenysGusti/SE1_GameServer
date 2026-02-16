package server.entities;

import jakarta.persistence.*;
import messagesbase.messagesfromclient.ETerrain;

import java.util.Objects;

@Entity
@Table(name = "half_map_nodes")
@IdClass(HalfMapNodeId.class)
public class HalfMapNodeEntity {
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participation_id", nullable = false, updatable = false)
    private PlayerParticipationEntity participation;

    @Id
    @Column(nullable = false, updatable = false)
    private int x;

    @Id
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
        return x == that.x && y == that.y && Objects.equals(participation, that.participation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(participation, x, y);
    }
}