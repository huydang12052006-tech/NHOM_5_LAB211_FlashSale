package model.Entity;

import java.time.LocalDateTime;
import model.BaseEntity.BaseEntity;

public class OrderDetail extends BaseEntity {

    private String orderId;
    private String flashItemId;
    private String productId;
    private int quantity;
    private double unitPrice;
    private double subTotal;

    public OrderDetail() {
    }

    /** Legacy constructor for flash-sale details. */
    public OrderDetail(String id, LocalDateTime createdAt, LocalDateTime updatedAt,
                       String orderId, String flashItemId, int quantity,
                       double unitPrice, double subTotal) {
        this(id, createdAt, updatedAt, orderId, flashItemId, null, quantity, unitPrice, subTotal);
    }

    public OrderDetail(String id, LocalDateTime createdAt, LocalDateTime updatedAt,
                       String orderId, String flashItemId, String productId, int quantity,
                       double unitPrice, double subTotal) {
        super(id, createdAt, updatedAt);
        this.orderId = orderId;
        this.flashItemId = flashItemId;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subTotal = subTotal;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getFlashItemId() { return flashItemId; }
    public void setFlashItemId(String flashItemId) { this.flashItemId = flashItemId; }
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
    public double getSubTotal() { return subTotal; }
    public void setSubTotal(double subTotal) { this.subTotal = subTotal; }

    @Override
    public String toCsvLine() {
        return String.join(",", escapeCsv(getId()), formatDateTime(getCreatedAt()),
                formatDateTime(getUpdatedAt()), escapeCsv(orderId), escapeCsv(flashItemId),
                escapeCsv(productId), String.valueOf(quantity), String.valueOf(unitPrice),
                String.valueOf(subTotal));
    }

    @Override
    public void fromCsvLine(String csv) {
        String[] parts = csv.split(",", -1);
        setId(parts[0]);
        setCreatedAt(LocalDateTime.parse(parts[1]));
        setUpdatedAt(LocalDateTime.parse(parts[2]));
        orderId = parts[3];
        flashItemId = emptyToNull(parts[4]);
        if (parts.length >= 9) {
            productId = emptyToNull(parts[5]);
            quantity = Integer.parseInt(parts[6]);
            unitPrice = Double.parseDouble(parts[7]);
            subTotal = Double.parseDouble(parts[8]);
        } else {
            productId = null;
            quantity = Integer.parseInt(parts[5]);
            unitPrice = Double.parseDouble(parts[6]);
            subTotal = Double.parseDouble(parts[7]);
        }
    }

    private String emptyToNull(String value) {
        return value == null || value.isEmpty() ? null : value;
    }

    @Override
    public String toString() {
        return "OrderDetail{" + "id='" + getId() + '\''
                + ", orderId='" + orderId + '\''
                + ", flashItemId='" + flashItemId + '\''
                + ", productId='" + productId + '\''
                + ", quantity=" + quantity + ", unitPrice=" + unitPrice
                + ", subTotal=" + subTotal + '}';
    }
}
