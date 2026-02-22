package server.validation.rule;

import server.data.HalfMap;
import server.validation.exception.HalfMapGenerationException;

import java.util.List;

public interface HalfMapValidationRule {
    List<HalfMapGenerationException> validate(HalfMap halfMap);
}
