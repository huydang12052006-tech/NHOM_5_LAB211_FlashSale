package model.Entity;

import java.time.LocalDateTime;
import model.BaseEntity.BaseEntity;
import model.Enum.SaleStatus;

public class Product extends BaseEntity {

    private String name;
    private String category;
    private double originalPrice;
    private int stockQty;
    private int version;
    private SaleStatus status;
    private String sellerId;

    public Product() {
        super();
        this.status = SaleStatus.ACTIVE;
    }

    public Product(String id, LocalDateTime createdAt, LocalDateTime updatedAt,
                   String name, String category, double originalPrice, int stockQty,
                   int version, SaleStatus status) {
        this(id, createdAt, updatedAt, name, category, originalPrice, stockQty,
                version, status, null);
    }

    public Product(String id, LocalDateTime createdAt, LocalDateTime updatedAt,
                   String name, String category, double originalPrice, int stockQty,
                   int version, SaleStatus status, String sellerId) {
        super(id, createdAt, updatedAt);
        this.name = name;
        this.category = category;
        this.originalPrice = originalPrice;
        this.stockQty = stockQty;
        this.version = version;
        this.status = status;
        this.sellerId = sellerId;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public double getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(double originalPrice) { this.originalPrice = originalPrice; }
    public int getStockQty() { return stockQty; }
    public void setStockQty(int stockQty) { this.stockQty = stockQty; }
    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }
    public SaleStatus getStatus() { return status; }
    public void setStatus(SaleStatus status) { this.status = status; }
    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }

    @Override
    public String toCsvLine() {
        return String.join(",", getId(), getCreatedAt().toString(), getUpdatedAt().toString(),
                name, category, String.valueOf(originalPrice), String.valueOf(stockQty),
                String.valueOf(version), status.name(), sellerId == null ? "" : sellerId);
    }

    @Override
    public void fromCsvLine(String csvLine) {
        String[] parts = csvLine.split(",", -1);
        setId(parts[0]);
        setCreatedAt(LocalDateTime.parse(parts[1]));
        setUpdatedAt(LocalDateTime.parse(parts[2]));
        setName(parts[3]);
        setCategory(parts[4]);
        setOriginalPrice(Double.parseDouble(parts[5]));
        setStockQty(Integer.parseInt(parts[6]));
        setVersion(Integer.parseInt(parts[7]));
        setStatus(SaleStatus.valueOf(parts[8]));
        setSellerId(parts.length > 9 ? parts[9] : null);
    }
}
