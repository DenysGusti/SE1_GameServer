package server.entities;

import java.io.Serializable;

public record HalfMapNodeId(String participation, int x, int y) implements Serializable {
}