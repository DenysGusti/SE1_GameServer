package server.game;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameIDGenerator {
	private static final String CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
	private static final int GAMEID_STRING_LENGTH = 5;
	private static final Logger logger = LoggerFactory.getLogger(GameIDGenerator.class);
	
	public static String generateGameID() {
		final StringBuilder gameIDBuilder = new StringBuilder();
		final Random random = new Random();
		
		for (int iteration = 0; iteration < GAMEID_STRING_LENGTH; iteration++) {
			final int index = random.nextInt(CHARSET.length());
			gameIDBuilder.append(CHARSET.charAt(index));
		}
		
		final String generatedGameID = gameIDBuilder.toString();
		logger.debug("Generated gameID: {}", generatedGameID);
		
		return generatedGameID;
	}

}
