package server.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "games")
public class GameEntity {
    @Id
    @Column(length = 5, nullable = false, unique = true, updatable = false)
    private String id;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false, updatable = false)
    private boolean debugMode;

    @Column(nullable = false, updatable = false)
    private boolean dummyCompetition;

    @Column(nullable = false, updatable = false)
    private boolean horizontalFullMap;

    @Column(nullable = false, updatable = false)
    private boolean firstPlayerTopOrLeftSide;

    @OneToMany(mappedBy = "game", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<PlayerParticipationEntity> playerParticipations = new ArrayList<>();

    @OneToMany(mappedBy = "game", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<GameStateEntity> gameStates = new ArrayList<>();

    @OneToMany(mappedBy = "game", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<FullMapNodeEntity> fullMapNodes = new ArrayList<>();

    protected GameEntity() {
    }

    public GameEntity(String id, boolean debugMode, boolean dummyCompetition, boolean horizontalFullMap,
                      boolean firstPlayerTopOrLeftSide, LocalDateTime createdAt) {
        if (id == null)
            throw new IllegalArgumentException("id is null");
        if (createdAt == null)
            throw new IllegalArgumentException("createdAt is null");

        this.id = id;
        this.debugMode = debugMode;
        this.dummyCompetition = dummyCompetition;
        this.horizontalFullMap = horizontalFullMap;
        this.firstPlayerTopOrLeftSide = firstPlayerTopOrLeftSide;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public boolean isDummyCompetition() {
        return dummyCompetition;
    }

    public boolean hasHorizontalFullMap() {
        return horizontalFullMap;
    }

    public boolean hasFirstPlayerTopOrLeftSide() {
        return firstPlayerTopOrLeftSide;
    }

    public List<PlayerParticipationEntity> getPlayerParticipations() {
        return List.copyOf(playerParticipations);
    }

    public List<GameStateEntity> getGameStates() {
        return List.copyOf(gameStates);
    }

    public List<FullMapNodeEntity> getFullMapNodes() {
        return List.copyOf(fullMapNodes);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        var that = (GameEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}