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
    private int version; // Bắt buộc cho Optimistic Lock của Member B
    private SaleStatus status; // Tích hợp Enum đồng bộ full hệ thống

    public FlashSaleItem() {
        super();
        this.status = SaleStatus.ACTIVE;
    }

    public FlashSaleItem(String id, LocalDateTime createdAt,
            LocalDateTime updatedAt,String eventId, String productId, double flashPrice, int limitedQty, int soldQty, int version, SaleStatus status) {
        super(id, createdAt, updatedAt);
        this.eventId = eventId;
        this.productId = productId;
        this.flashPrice = flashPrice;
        this.limitedQty = limitedQty;
        this.soldQty = soldQty;
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
                this.getId(), this.eventId, this.productId, String.valueOf(this.flashPrice),
                String.valueOf(this.limitedQty), String.valueOf(this.soldQty), String.valueOf(this.version),
                this.status.name(),
                this.getCreatedAt().toString(), this.getUpdatedAt().toString()
        );
    }
    @Override
    public void fromCsvLine(String csvLine) {
        String[] parts = csvLine.split(",");
        this.setId(parts[0]);
        this.setEventId(parts[1]);
        this.setProductId(parts[2]);
        this.setFlashPrice(Double.parseDouble(parts[3]));
        this.setLimitedQty(Integer.parseInt(parts[4]));
        this.setSoldQty(Integer.parseInt(parts[5]));
        this.setVersion(Integer.parseInt(parts[6]));
        this.setStatus(SaleStatus.valueOf(parts[7]));
        this.setCreatedAt(LocalDateTime.parse(parts[8]));
        this.setUpdatedAt(LocalDateTime.parse(parts[9]));
    }
}
