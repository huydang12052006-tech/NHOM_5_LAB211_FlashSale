package model.Entity;

import model.Enum.CustomerTier;

import java.time.LocalDateTime;

import model.BaseEntity.BaseEntity;


public class Customer extends BaseEntity {

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
                    String fullName,
                    String phone,
                    String email,
                    CustomerTier tier,
                    double totalSpent,
                    boolean active) {

        super(id,createdAt,updatedAt);

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
                escapeCsv(fullName),
                escapeCsv(phone),
                escapeCsv(email),
                tier.name(),
                String.valueOf(totalSpent),
                String.valueOf(active),
                formatDateTime(getCreatedAt()),
                formatDateTime(getUpdatedAt())
        );
    }

    @Override
    public void fromCsvLine(String csv) {

        String[] parts = csv.split(",", -1);

        setId(parts[0]);

        setCreatedAt(
                parseDateTime(parts[7])
        );

        setUpdatedAt(
                parseDateTime(parts[8])
        );

        this.fullName = parts[1];

        this.phone = parts[2];

        this.email = parts[3];

        this.tier =
                CustomerTier.valueOf(parts[4]);

        this.totalSpent =
                Double.parseDouble(parts[5]);

        this.active =
                Boolean.parseBoolean(parts[6]);
    }

    // =========================
    // toString
    // =========================

    @Override
    public String toString() {

        return "Customer{" +
                "id='" + getId() + '\'' +
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