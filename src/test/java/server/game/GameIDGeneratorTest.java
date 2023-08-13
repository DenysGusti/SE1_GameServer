package server.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;

public class GameIDGeneratorTest {
	private static final String CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
	private static final int GAMEID_STRING_LENGTH = 5;	
    @Test
    public void checkGameIDLength() {
    	for (int iteration = 0; iteration < 100; iteration++) {
            assertEquals(GAMEID_STRING_LENGTH, GameIDGenerator.generateGameID().length());
        }
    }

    @Test
    public void checkGameIDCharset() {
    	final Set<Integer> allowedChars = CHARSET.chars().boxed().collect(Collectors.toUnmodifiableSet());
        for (int iteration = 0; iteration < 100; iteration++) {
            final String gameID = GameIDGenerator.generateGameID();
            final OptionalInt illegalChar = gameID.chars().filter(eachChar -> !allowedChars.contains(eachChar)).findAny();
            
            assertEquals(illegalChar, OptionalInt.empty());
        }
    }	
    
    @Test
    public void checkGameIDUniqueness() {
        for (int iteration = 0; iteration < 100; iteration++) {
            final String firstGeneratedGameID = GameIDGenerator.generateGameID();
            final String secondGeneratedGameID = GameIDGenerator.generateGameID();

            assertFalse(firstGeneratedGameID.equals(secondGeneratedGameID));
        }
    }
}

