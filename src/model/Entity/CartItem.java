package model.Entity;

import java.time.LocalDateTime;
import model.BaseEntity.BaseEntity;

/** A persisted customer cart line. Exactly one of flashItemId/productId is required. */
public class CartItem extends BaseEntity {

    private String customerId;
    private String flashItemId;
    private String productId;
    private int quantity;

    public CartItem() {
    }

    public CartItem(String id, LocalDateTime createdAt, LocalDateTime updatedAt,
                    String customerId, String flashItemId, String productId, int quantity) {
        super(id, createdAt, updatedAt);
        this.customerId = customerId;
        this.flashItemId = flashItemId;
        this.productId = productId;
        this.quantity = quantity;
    }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public String getFlashItemId() { return flashItemId; }
    public void setFlashItemId(String flashItemId) { this.flashItemId = flashItemId; }
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    @Override
    public String toCsvLine() {
        return String.join(",", escapeCsv(getId()), formatDateTime(getCreatedAt()),
                formatDateTime(getUpdatedAt()), escapeCsv(customerId), escapeCsv(flashItemId),
                escapeCsv(productId), String.valueOf(quantity));
    }

    @Override
    public void fromCsvLine(String csv) {
        String[] parts = csv.split(",", -1);
        setId(parts[0]);
        setCreatedAt(LocalDateTime.parse(parts[1]));
        setUpdatedAt(LocalDateTime.parse(parts[2]));
        customerId = parts[3];
        flashItemId = parts[4].isEmpty() ? null : parts[4];
        productId = parts[5].isEmpty() ? null : parts[5];
        quantity = Integer.parseInt(parts[6]);
    }
}
