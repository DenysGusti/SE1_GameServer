package server.entity;

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

    public void setFirstName(String firstName) {
        if (firstName == null)
            throw new IllegalArgumentException("firstName is null");

        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        if (lastName == null)
            throw new IllegalArgumentException("lastName is null");

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