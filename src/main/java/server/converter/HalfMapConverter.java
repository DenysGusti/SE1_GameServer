package server.converter;

import messagesbase.messagesfromclient.ETerrain;
import messagesbase.messagesfromclient.PlayerHalfMap;
import messagesbase.messagesfromclient.PlayerHalfMapNode;
import server.data.HalfMap;
import server.data.XYPair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class HalfMapConverter {
    public HalfMap convertHalfMap(PlayerHalfMap playerHalfMap) {
        if (playerHalfMap == null)
            throw new IllegalArgumentException("playerHalfMap is null");

        Map<XYPair, ETerrain> nodes = new HashMap<>();
        Set<XYPair> potentialForts = new HashSet<>();

        for (PlayerHalfMapNode node : playerHalfMap.getMapNodes()) {
            var coordinate = new XYPair(node.getX(), node.getY());
            nodes.put(coordinate, node.getTerrain());
            if (node.isFortPresent())
                potentialForts.add(coordinate);
        }

        return new HalfMap(nodes, potentialForts);
    }
}
