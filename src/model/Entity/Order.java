/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model.Entity;

import model.BaseEntity.BaseEntity;
import model.Enum.LockMechanism;
import model.Enum.OrderStatus;

import java.time.LocalDateTime;
// import java.time.format.DateTimeFormatter;

public class Order extends BaseEntity {

    // private static final DateTimeFormatter FORMATTER
    //         = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private String customerId;
    private String eventId;
    private double totalAmount;
    private OrderStatus status;
    private LockMechanism lockMechanism;

    // Default constructor
    public Order() {
    }

    // Full constructor
    public Order(String id,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            String customerId,
            String eventId,
            double totalAmount,
            OrderStatus status,
            LockMechanism lockMechanism) {

        super(id, createdAt, updatedAt);
        this.customerId = customerId;
        this.eventId = eventId;
        this.totalAmount = totalAmount;
        this.status = status;
        this.lockMechanism = lockMechanism;

    }

    // =========================
    // Getter & Setter
    // =========================
    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public LockMechanism getLockMechanism() {
        return lockMechanism;
    }

    public void setLockMechanism(LockMechanism lockMechanism) {
        this.lockMechanism = lockMechanism;
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
            escapeCsv(customerId),
            escapeCsv(eventId),
            String.valueOf(totalAmount),
            status.name(),
            lockMechanism.name()           
    );
}

@Override
public void fromCsvLine(String csv) {

    String[] parts = csv.split(",", -1);

    setId(parts[0]);

    super.setCreatedAt(parseDateTime(parts[2]));

    super.setUpdatedAt(parseDateTime(parts[3]));

    this.customerId = parts[4];

    this.eventId = parts[5];

    this.totalAmount = Double.parseDouble(parts[6]);

    this.status = OrderStatus.valueOf(parts[7]);

    this.lockMechanism =
            LockMechanism.valueOf(parts[8]);

    
}

@Override
public String toString() {

    return "Order{" +
            "id='" + getId() + '\'' +
            ", customerId='" + customerId + '\'' +
            ", eventId='" + eventId + '\'' +
            ", totalAmount=" + totalAmount +
            ", status=" + status +
            ", lockMechanism=" + lockMechanism +
            ", createdAt=" + getCreatedAt() +
            ", updatedAt=" + getUpdatedAt() +
            '}';
}

}
