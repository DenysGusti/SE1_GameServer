package server.entities;

import java.io.Serializable;

public record PlayerStateId(String participation, String gameState) implements Serializable {
}