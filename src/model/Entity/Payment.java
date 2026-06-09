package model.Entity;

import model.Enum.PaymentMethod;

import java.time.LocalDateTime;

import model.BaseEntity.BaseEntity;


public class Payment extends BaseEntity {

    private String orderId;

    private String customerId;

    private PaymentMethod paymentMethod;

    private double amount;

    // =========================
    // Constructors
    // =========================

    public Payment() {
    }

    public Payment(String id,
                   LocalDateTime createdAt,
                   LocalDateTime updatedAt,
                   String orderId,
                   String customerId,
                   PaymentMethod paymentMethod,
                   double amount) {

        super(id, createdAt, updatedAt);

        this.orderId = orderId;
        this.customerId = customerId;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
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

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
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
                escapeCsv(orderId),
                escapeCsv(customerId),
                paymentMethod.name(),
                String.valueOf(amount)
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

        this.customerId = parts[4];

        this.paymentMethod =
                PaymentMethod.valueOf(parts[5]);

        this.amount =
                Double.parseDouble(parts[6]);
    }

    // =========================
    // toString
    // =========================

    @Override
    public String toString() {

        return "Payment{" +
                "id='" + getId() + '\'' +
                ", orderId='" + orderId + '\'' +
                ", customerId='" + customerId + '\'' +
                ", paymentMethod=" + paymentMethod +
                ", amount=" + amount +
                ", createdAt=" + getCreatedAt() +
                ", updatedAt=" + getUpdatedAt() +
                '}';
    }
}
