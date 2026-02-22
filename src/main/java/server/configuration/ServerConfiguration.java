package server.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import server.converter.HalfMapConverter;
import server.validation.HalfMapValidator;
import server.validation.rule.*;

import java.util.Set;
import java.util.random.RandomGenerator;

@Configuration
public class ServerConfiguration {
    @Bean
    public RandomGenerator randomGenerator() {
        return RandomGenerator.getDefault();
    }

    @Bean
    public HalfMapValidator halfMapValidator() {
        Set<HalfMapValidationRule> rules = Set.of(
                new TerrainRule(), new BorderRule(), new FortRule(), new ConnectivityRule()
        );
        return new HalfMapValidator(rules);
    }

    @Bean
    public HalfMapConverter halfMapConverter() {
        return new HalfMapConverter();
    }
}