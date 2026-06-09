package model.Entity;

import java.time.LocalDateTime;
import model.BaseEntity.BaseEntity;
import model.Enum.SaleStatus;


public class FlashSaleItem extends BaseEntity {

    private String eventId;
    private String productId;
    private double flashPrice;
    private int limitedQty;
    private int soldQty;
    private double discountPercent;
    private int version; // Bắt buộc cho Optimistic Lock của Member B
    private SaleStatus status; // Tích hợp Enum đồng bộ full hệ thống

    public FlashSaleItem() {
        super();
        this.status = SaleStatus.ACTIVE;
    }

    public FlashSaleItem(String id, LocalDateTime createdAt,
            LocalDateTime updatedAt, String eventId, String productId, double flashPrice,
            int limitedQty, int soldQty, double discountPercent, int version, SaleStatus status) {
        super(id, createdAt, updatedAt);
        this.eventId = eventId;
        this.productId = productId;
        this.flashPrice = flashPrice;
        this.limitedQty = limitedQty;
        this.soldQty = soldQty;
        this.discountPercent = discountPercent;
        this.version = version;
        this.status = status;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public double getFlashPrice() {
        return flashPrice;
    }

    public void setFlashPrice(double flashPrice) {
        this.flashPrice = flashPrice;
    }

    public int getLimitedQty() {
        return limitedQty;
    }

    public void setLimitedQty(int limitedQty) {
        this.limitedQty = limitedQty;
    }

    public int getSoldQty() {
        return soldQty;
    }

    public void setSoldQty(int soldQty) {
        this.soldQty = soldQty;
    }

    public double getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(double discountPercent) {
        this.discountPercent = discountPercent;
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
                this.eventId,
                this.productId,
                String.valueOf(this.flashPrice),
                String.valueOf(this.limitedQty),
                String.valueOf(this.soldQty),
                String.valueOf(this.discountPercent),
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
        this.setEventId(parts[3]);
        this.setProductId(parts[4]);
        this.setFlashPrice(Double.parseDouble(parts[5]));
        this.setLimitedQty(Integer.parseInt(parts[6]));
        this.setSoldQty(Integer.parseInt(parts[7]));
        this.setDiscountPercent(Double.parseDouble(parts[8]));
        this.setVersion(Integer.parseInt(parts[9]));
        this.setStatus(SaleStatus.valueOf(parts[10]));
        
    }
}
