package server.entities;

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

    private boolean debugMode;
    private boolean dummyCompetition;

    private String fullMapType;

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<PlayerParticipationEntity> participations = new ArrayList<>();

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<GameStateEntity> gameStates = new ArrayList<>();

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<FullMapNodeEntity> fullMapNodes = new ArrayList<>();

    protected GameEntity() {
    }

    public GameEntity(String id, boolean debugMode, boolean dummyCompetition) {
        this.id = id;
        this.debugMode = debugMode;
        this.dummyCompetition = dummyCompetition;
        createdAt = LocalDateTime.now();
    }

    public void addParticipation(PlayerParticipationEntity participation) {
        participations.add(participation);
    }

    public void addGameState(GameStateEntity state) {
        gameStates.add(state);
    }

    public void addFullMapNode(FullMapNodeEntity node) {
        fullMapNodes.add(node);
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

    public String getFullMapType() {
        return fullMapType;
    }

    public List<PlayerParticipationEntity> getParticipations() {
        return participations;
    }

    public List<GameStateEntity> getGameStates() {
        return gameStates;
    }

    public List<FullMapNodeEntity> getFullMapNodes() {
        return fullMapNodes;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    public void setDummyCompetition(boolean dummyCompetition) {
        this.dummyCompetition = dummyCompetition;
    }

    public void setFullMapType(String fullMapType) {
        this.fullMapType = fullMapType;
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