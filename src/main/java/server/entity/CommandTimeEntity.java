package server.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "command_times")
public class CommandTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, unique = true, updatable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "playerId", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private PlayerParticipationEntity playerParticipation;

    @Column(nullable = false, updatable = false)
    private LocalDateTime commandAt;

    protected CommandTimeEntity() {
    }

    public CommandTimeEntity(PlayerParticipationEntity playerParticipation, LocalDateTime commandAt) {
        if (playerParticipation == null)
            throw new IllegalArgumentException("playerParticipation is null");
        if (commandAt == null)
            throw new IllegalArgumentException("commandAt is null");

        this.playerParticipation = playerParticipation;
        this.commandAt = commandAt;
    }

    public String getId() {
        return id;
    }

    public PlayerParticipationEntity getPlayerParticipation() {
        return playerParticipation;
    }

    public LocalDateTime getCommandAt() {
        return commandAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        var that = (CommandTimeEntity) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
