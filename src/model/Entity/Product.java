package model.Entity;

import java.time.LocalDateTime;
import model.BaseEntity.BaseEntity;
import model.Enum.SaleStatus;

public class Product extends BaseEntity {

    private String name;
    private String category;
    private double originalPrice;
    private int stockQty;
    private int version; // Bắt buộc cho Optimistic Lock của Member B
    private SaleStatus status; // Chuẩn Enum full hệ thống thay cho boolean active

    public Product() {
        super();
        this.status = SaleStatus.ACTIVE; // Mặc định khi tạo mới là ACTIVE
    }

    public Product(String id, LocalDateTime createdAt,
            LocalDateTime updatedAt,String name, String category, double originalPrice, int stockQty, int version, SaleStatus status) {
        super(id, createdAt, updatedAt);
        this.name = name;
        this.category = category;
        this.originalPrice = originalPrice;
        this.stockQty = stockQty;
        this.version = version;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(double originalPrice) {
        this.originalPrice = originalPrice;
    }

    public int getStockQty() {
        return stockQty;
    }

    public void setStockQty(int stockQty) {
        this.stockQty = stockQty;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public SaleStatus getStatus() {
        return status;
    }

    public void setStatus(SaleStatus status) {
        this.status = status;
    }

    @Override
    public String toCsvLine() {
        return String.join(",",
                this.getId(),
                this.getCreatedAt().toString(),
                this.getUpdatedAt().toString(),
                this.name,
                this.category,
                String.valueOf(this.originalPrice),
                String.valueOf(this.stockQty),
                String.valueOf(this.version),
                this.status.name()
        );
    }
    @Override
    public void fromCsvLine(String csvLine) {
        String[] parts = csvLine.split(",");
        this.setId(parts[0]);
        this.setCreatedAt(LocalDateTime.parse(parts[1]));
        this.setUpdatedAt(LocalDateTime.parse(parts[2]));
        this.setName(parts[3]);
        this.setCategory(parts[4]);
        this.setOriginalPrice(Double.parseDouble(parts[5]));
        this.setStockQty(Integer.parseInt(parts[6]));
        this.setVersion(Integer.parseInt(parts[7]));
        this.setStatus(SaleStatus.valueOf(parts[8])); // Đọc Enum từ chuỗi CSV
        
     
    }
}
