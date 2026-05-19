/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model.entity;

import model.enums.LockMechanism;
import model.enums.OrderStatus;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Order {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private String id;
    private String customerId;
    private String eventId;
    private double totalAmount;
    private OrderStatus status;
    private LockMechanism lockMechanism;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Default constructor
    public Order() {
    }

    // Full constructor
    public Order(String id,
                 String customerId,
                 String eventId,
                 double totalAmount,
                 OrderStatus status,
                 LockMechanism lockMechanism,
                 LocalDateTime createdAt,
                 LocalDateTime updatedAt) {

        this.id = id;
        this.customerId = customerId;
        this.eventId = eventId;
        this.totalAmount = totalAmount;
        this.status = status;
        this.lockMechanism = lockMechanism;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // =========================
    // Getter & Setter
    // =========================

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // =========================
    // CSV Methods
    // =========================

    public String toCsvLine() {
        return String.join(",",
                escape(id),
                escape(customerId),
                escape(eventId),
                String.valueOf(totalAmount),
                status.name(),
                lockMechanism.name(),
                createdAt.format(FORMATTER),
                updatedAt.format(FORMATTER)
        );
    }

    public static Order fromCsvLine(String csvLine) {

        String[] parts = csvLine.split(",", -1);

        return new Order(
                parts[0],
                parts[1],
                parts[2],
                Double.parseDouble(parts[3]),
                OrderStatus.valueOf(parts[4]),
                LockMechanism.valueOf(parts[5]),
                LocalDateTime.parse(parts[6], FORMATTER),
                LocalDateTime.parse(parts[7], FORMATTER)
        );
    }

    // =========================
    // Helper
    // =========================

    private static String escape(String value) {
        return value == null ? "" : value.replace(",", " ");
    }

    @Override
    public String toString() {
        return "Order{" +
                "id='" + id + '\'' +
                ", customerId='" + customerId + '\'' +
                ", eventId='" + eventId + '\'' +
                ", totalAmount=" + totalAmount +
                ", status=" + status +
                ", lockMechanism=" + lockMechanism +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
