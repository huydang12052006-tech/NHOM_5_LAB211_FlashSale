/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model.BaseEntity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public abstract class BaseEntity {

    protected static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private String id;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // =========================
    // Constructors
    // =========================

    public BaseEntity() {
    }

    public BaseEntity(String id, LocalDateTime createdAt,LocalDateTime updatedAt) {

        this.id = id;
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

    /*
        Convert object -> CSV line
     */
    public abstract String toCsvLine();

    /*
        Parse CSV -> object

        mỗi entity sẽ tự override:
        Product.fromCsvLine()
        Order.fromCsvLine()
        ...
     */
    public abstract void fromCsvLine(String csv);

    // =========================
    // Common Helper Methods
    // =========================

    protected String formatDateTime(LocalDateTime dateTime) {

        if (dateTime == null) {
            return "";
        }

        return dateTime.format(FORMATTER);
    }

    protected LocalDateTime parseDateTime(String value) {

        if (value == null) {
            return null;
        }

        return LocalDateTime.parse(value, FORMATTER);
    }

    protected String escapeCsv(String value) {

        if (value == null) {
            return "";
        }

        return value.replace(",", " ");
    }

    @Override
    public String toString() {
        return "BaseEntity{" +
                "id='" + id + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
