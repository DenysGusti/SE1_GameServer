package server.entities;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "players")
public class PlayerEntity {
    @Id
    @Column(nullable = false, unique = true, updatable = false)
    private String uAccount;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    protected PlayerEntity() {
    }

    public PlayerEntity(String uAccount, String firstName, String lastName) {
        this.uAccount = uAccount;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public void updateName(String firstName, String lastName) {
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

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        var that = (PlayerEntity) o;
        return Objects.equals(uAccount, that.uAccount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uAccount);
    }
}