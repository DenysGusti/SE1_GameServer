package server.player;

import java.time.LocalDateTime;

import server.player.enumeration.EServerState;

public class Player implements Cloneable {
	private boolean hasTreasure;
	private final String firstName;
	private final String lastName;
	private EServerState state;
	private final String uaccount;
	private String playerID;
	private LocalDateTime lastActionTime;

	public Player(final boolean hasTreasure, final String firstName, final String lastName, final EServerState state,
			final String uaccount, final String playerID, final LocalDateTime lastActionTime) {
		this.hasTreasure = hasTreasure;
		this.firstName = firstName;
		this.lastName = lastName;
		this.state = state;
		this.uaccount = uaccount;
		this.playerID = playerID;
		this.lastActionTime = lastActionTime;
	}

	public Player(final String firstName, final String lastName, final String uaccount, final String playerID) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.uaccount = uaccount;
		this.playerID = playerID;
	}

	public Player(final boolean hasTreasure, final String firstName, final String lastName, final EServerState state,
			final String uaccount, final String playerID) {
		this.hasTreasure = hasTreasure;
		this.firstName = firstName;
		this.lastName = lastName;
		this.state = state;
		this.uaccount = uaccount;
		this.playerID = playerID;
	}

	public String getPlayerID() {
		return playerID;
	}

	public void setPlayerID(final String playerID) {
		this.playerID = playerID;
	}

	public boolean isHasTreasure() {
		return hasTreasure;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public EServerState getState() {
		return state;
	}

	public void setState(final EServerState state) {
		this.state = state;
	}

	public String getUaccount() {
		return uaccount;
	}

	public LocalDateTime getLastActionTime() {
		return lastActionTime;
	}

	public void performAction(final LocalDateTime lastActionTime) {
		this.lastActionTime = lastActionTime;
	}

	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			return new Player(this.hasTreasure, this.firstName, this.lastName, this.state, this.uaccount, this.playerID,
					this.lastActionTime);
		}
	}
}
