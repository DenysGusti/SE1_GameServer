package server.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "players")
public class PlayerRegistrationEntity {
    @Id
    @Column(nullable = false, unique = true, updatable = false)
    private String uAccount;

    @Column(nullable = false, updatable = false)
    private String firstName;

    @Column(nullable = false, updatable = false)
    private String lastName;

    @OneToMany(mappedBy = "playerRegistration", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private final List<PlayerParticipationEntity> playerParticipations = new ArrayList<>();

    protected PlayerRegistrationEntity() {
    }

    public PlayerRegistrationEntity(String uAccount, String firstName, String lastName) {
        if (uAccount == null)
            throw new IllegalArgumentException("uAccount is null");
        if (firstName == null)
            throw new IllegalArgumentException("firstName is null");
        if (lastName == null)
            throw new IllegalArgumentException("lastName is null");

        this.uAccount = uAccount;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getUAccount() {
        return uAccount;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public List<PlayerParticipationEntity> getPlayerParticipations() {
        return List.copyOf(playerParticipations);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        var that = (PlayerRegistrationEntity) o;
        return Objects.equals(uAccount, that.uAccount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uAccount);
    }
}