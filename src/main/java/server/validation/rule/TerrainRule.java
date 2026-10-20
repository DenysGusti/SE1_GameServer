package server.validation.rule;

import messagesbase.messagesfromclient.ETerrain;
import server.data.HalfMap;
import server.data.XYPair;
import server.validation.exception.HalfMapGenerationException;
import server.validation.exception.TerrainRuleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class TerrainRule implements HalfMapValidationRule {
    private static final Logger logger = LoggerFactory.getLogger(TerrainRule.class);

    private static final XYPair HALF_MAP_SIZE = new XYPair(10, 5);
    private static final int HALF_MAP_NODES = 50;
    private static final int MIN_MOUNTAIN_NODES = 5;
    private static final int MIN_GRASS_NODES = 24;
    private static final int MIN_WATER_NODES = 7;

    @Override
    public List<HalfMapGenerationException> validate(HalfMap halfMap) {
        if (halfMap == null)
            throw new IllegalArgumentException("halfMap is null");

        List<HalfMapGenerationException> errors = new ArrayList<>();

        if (halfMap.nodes().size() != HALF_MAP_NODES)
            errors.add(new TerrainRuleException("Map must have exactly 50 nodes, but found " + halfMap.nodes().size()));

        for (XYPair coordinate : halfMap.nodes().keySet())
            if (coordinate.x() < 0 || coordinate.x() >= HALF_MAP_SIZE.x() ||
                    coordinate.y() < 0 || coordinate.y() >= HALF_MAP_SIZE.y())
                errors.add(new TerrainRuleException("Node coordinate out of half-map bounds (0..9, 0..4): " + coordinate));

        long mountainCount = halfMap.nodes().values().stream().filter(t -> t == ETerrain.Mountain).count();
        if (mountainCount < MIN_MOUNTAIN_NODES)
            errors.add(new TerrainRuleException("Not enough mountains. Found " + mountainCount));

        long grassCount = halfMap.nodes().values().stream().filter(t -> t == ETerrain.Grass).count();
        if (grassCount < MIN_GRASS_NODES)
            errors.add(new TerrainRuleException("Not enough grass. Found " + grassCount));

        long waterCount = halfMap.nodes().values().stream().filter(t -> t == ETerrain.Water).count();
        if (waterCount < MIN_WATER_NODES)
            errors.add(new TerrainRuleException("Not enough water. Found " + waterCount));

        return errors;
    }
}
