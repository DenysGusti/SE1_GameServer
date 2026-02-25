package server.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;
import java.util.Objects;


@Entity
@Table(name = "query_times")
public class QueryTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, unique = true, updatable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "playerId", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private PlayerParticipationEntity playerParticipation;

    @Column(nullable = false, updatable = false)
    private LocalDateTime queryAt;

    protected QueryTimeEntity() {
    }

    public QueryTimeEntity(PlayerParticipationEntity playerParticipation, LocalDateTime queryAt) {
        if (playerParticipation == null)
            throw new IllegalArgumentException("playerParticipation is null");
        if (queryAt == null)
            throw new IllegalArgumentException("queryAt is null");

        this.playerParticipation = playerParticipation;
        this.queryAt = queryAt;
    }

    public String getId() {
        return id;
    }

    public PlayerParticipationEntity getPlayerParticipation() {
        return playerParticipation;
    }

    public LocalDateTime getQueryAt() {
        return queryAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        var that = (QueryTimeEntity) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
