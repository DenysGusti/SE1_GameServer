package server.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.random.RandomGenerator;

@Configuration
public class RandomConfiguration {
    @Bean
    public RandomGenerator randomGenerator() {
        return RandomGenerator.getDefault();
    }
}