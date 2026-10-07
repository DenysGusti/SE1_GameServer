package server.converter;

import server.data.FullMap;
import server.data.FullMapNode;
import server.data.XYPair;
import server.entity.FullMapNodeEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FullMapConverter {
    public FullMap convertFullMap(List<FullMapNodeEntity> fullMapNodes) {
        if (fullMapNodes == null)
            throw new IllegalArgumentException("fullMapNodes is null");

        if (fullMapNodes.isEmpty())
            return FullMap.emptyFullMap();

        Map<XYPair, FullMapNode> nodes = new HashMap<>();
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (FullMapNodeEntity entity : fullMapNodes) {
            var coordinate = new XYPair(entity.getX(), entity.getY());
            nodes.put(coordinate, new FullMapNode(entity.getTerrain(), false));

            minX = Math.min(minX, entity.getX());
            minY = Math.min(minY, entity.getY());
            maxX = Math.max(maxX, entity.getX());
            maxY = Math.max(maxY, entity.getY());
        }

        return new FullMap(
                nodes,
                new XYPair(minX, minY),
                new XYPair(maxX, maxY),
                null, null, null, null, null, false
        );
    }
}
