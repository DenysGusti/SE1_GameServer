package server.entities;

import java.io.Serializable;

public record FullMapNodeId(String game, int x, int y) implements Serializable {
}