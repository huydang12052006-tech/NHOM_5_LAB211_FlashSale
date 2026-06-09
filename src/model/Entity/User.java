package model.Entity;

import model.Enum.UserRole;

import java.time.LocalDateTime;

import model.BaseEntity.BaseEntity;


public class User extends BaseEntity {

    private String username;

    private String passwordHash;

    private UserRole role;

    private boolean active;

    // =========================
    // Constructors
    // =========================

    public User() {
    }

    public User(String id,
                LocalDateTime createdAt,
                LocalDateTime updatedAt,
                String username,
                String passwordHash,
                UserRole role,
                boolean active) {

        super(id, createdAt, updatedAt);

        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.active = active;
    }

    // =========================
    // Getter & Setter
    // =========================

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    // =========================
    // CSV Methods
    // =========================

    @Override
    public String toCsvLine() {

        return String.join(",",
                escapeCsv(getId()),
                formatDateTime(getCreatedAt()),
                formatDateTime(getUpdatedAt()),
                escapeCsv(username),
                escapeCsv(passwordHash),
                role.name(),
                String.valueOf(active)
        );
    }

    @Override
    public void fromCsvLine(String csv) {

        String[] parts = csv.split(",", -1);

        setId(parts[0]);

        setCreatedAt(
                LocalDateTime.parse(parts[1])
        );

        setUpdatedAt(
                LocalDateTime.parse(parts[2])
        );

        this.username = parts[3];

        this.passwordHash = parts[4];

        this.role =
                UserRole.valueOf(parts[5]);

        this.active =
                Boolean.parseBoolean(parts[6]);
    }

    // =========================
    // toString
    // =========================

    @Override
    public String toString() {

        return "User{" +
                "id='" + getId() + '\'' +
                ", username='" + username + '\'' +
                ", role=" + role +
                ", active=" + active +
                ", createdAt=" + getCreatedAt() +
                ", updatedAt=" + getUpdatedAt() +
                '}';
    }
}
