package model.Entity;

import model.Enum.CustomerTier;

import java.time.LocalDateTime;

import model.BaseEntity.BaseEntity;


public class Customer extends BaseEntity {

    private String userId;

    private String fullName;

    private String phone;

    private String email;

    private CustomerTier tier;

    private double totalSpent;

    private boolean active;

    // =========================
    // Constructors
    // =========================
    public Customer(){

    }

    public Customer(String id,
                    LocalDateTime createdAt,
                    LocalDateTime updatedAt,
                    String userId,
                    String fullName,
                    String phone,
                    String email,
                    CustomerTier tier,
                    double totalSpent,
                    boolean active) {

        super(id,createdAt,updatedAt);

        this.userId = userId;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.tier = tier;
        this.totalSpent = totalSpent;
        this.active = active;
    }

    // =========================
    // Getter & Setter
    // =========================

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public CustomerTier getTier() {
        return tier;
    }

    public void setTier(CustomerTier tier) {
        this.tier = tier;
    }

    public double getTotalSpent() {
        return totalSpent;
    }

    public void setTotalSpent(double totalSpent) {
        this.totalSpent = totalSpent;
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
                escapeCsv(userId),
                escapeCsv(fullName),
                escapeCsv(phone),
                escapeCsv(email),
                tier.name(),
                String.valueOf(totalSpent),
                String.valueOf(active)
        );
    }

    @Override
    public void fromCsvLine(String csv) {

        String[] parts = csv.split(",", -1);
        int offset = parts.length >= 10 ? 1 : 0;

        setId(parts[0]);

        setCreatedAt(
                LocalDateTime.parse(parts[1])
        );

        setUpdatedAt(
                LocalDateTime.parse(parts[2])
        );

        this.userId = offset == 1 ? parts[3] : null;

        this.fullName = parts[3 + offset];

        this.phone = parts[4 + offset];

        this.email = parts[5 + offset];

        this.tier =
                CustomerTier.valueOf(parts[6 + offset]);

        this.totalSpent =
                Double.parseDouble(parts[7 + offset]);

        this.active =
                Boolean.parseBoolean(parts[8 + offset]);
    }

    // =========================
    // toString
    // =========================

    @Override
    public String toString() {

        return "Customer{" +
                "id='" + getId() + '\'' +
                ", userId='" + userId + '\'' +
                ", fullName='" + fullName + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", tier=" + tier +
                ", totalSpent=" + totalSpent +
                ", active=" + active +
                ", createdAt=" + getCreatedAt() +
                ", updatedAt=" + getUpdatedAt() +
                '}';
    }
}
