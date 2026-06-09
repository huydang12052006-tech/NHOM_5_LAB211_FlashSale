package model.Entity;
import java.time.LocalDateTime;

import model.BaseEntity.BaseEntity;

public class OrderDetail extends BaseEntity {

    private String orderId;

    private String flashItemId;

    private int quantity;

    private double unitPrice;

    private double subTotal;

    // =========================
    // Constructors
    // =========================

    public OrderDetail() {
    }

    public OrderDetail(String id,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
                       String orderId,
                       String flashItemId,
                       int quantity,
                       double unitPrice,
                       double subTotal) {

        super(id, createdAt, updatedAt);
        this.orderId = orderId;
        this.flashItemId = flashItemId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subTotal = subTotal;
    }

    // =========================
    // Getter & Setter
    // =========================

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getFlashItemId() {
        return flashItemId;
    }

    public void setFlashItemId(String flashItemId) {
        this.flashItemId = flashItemId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public double getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(double subTotal) {
        this.subTotal = subTotal;
    }

    // =========================
    // CSV Methods
    // =========================

    @Override
    public String toCsvLine() {

        return String.join(",",
                escapeCsv(getId()),
                escapeCsv(orderId),
                escapeCsv(flashItemId),
                String.valueOf(quantity),
                String.valueOf(unitPrice),
                String.valueOf(subTotal),
                formatDateTime(getCreatedAt()),
                formatDateTime(getUpdatedAt())
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

        this.orderId = parts[3];

        this.flashItemId = parts[4];

        this.quantity = Integer.parseInt(parts[5]);

        this.unitPrice = Double.parseDouble(parts[6]);

        this.subTotal = Double.parseDouble(parts[7]);

        
    }

    // =========================
    // toString
    // =========================

    @Override
    public String toString() {

        return "OrderDetail{" +
                "id='" + getId() + '\'' +
                ", orderId='" + orderId + '\'' +
                ", flashItemId='" + flashItemId + '\'' +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", subTotal=" + subTotal +
                ", createdAt=" + getCreatedAt() +
                ", updatedAt=" + getUpdatedAt() +
                '}';
    }
}
