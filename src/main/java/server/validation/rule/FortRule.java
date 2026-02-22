package server.validation.rule;

import messagesbase.messagesfromclient.ETerrain;
import server.data.XYPair;
import server.data.HalfMap;
import server.validation.exception.FortRuleException;
import server.validation.exception.HalfMapGenerationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class FortRule implements HalfMapValidationRule {
    private static final Logger logger = LoggerFactory.getLogger(FortRule.class);

    private static final int REQUIRED_FORTS = 1;

    @Override
    public List<HalfMapGenerationException> validate(HalfMap halfMap) {
        if (halfMap == null)
            throw new IllegalArgumentException("halfMap is null");

        List<HalfMapGenerationException> errors = new ArrayList<>();

        if (halfMap.potentialForts().size() != REQUIRED_FORTS)
            errors.add(new FortRuleException("Wrong number of forts. Found " + halfMap.potentialForts().size() + ", Required " + REQUIRED_FORTS));

        for (XYPair fortPosition : halfMap.potentialForts())
            if (halfMap.nodes().get(fortPosition) != ETerrain.Grass)
                errors.add(new FortRuleException("A fort was placed on a non-Grass tile at " + fortPosition));

        return errors;
    }
}
