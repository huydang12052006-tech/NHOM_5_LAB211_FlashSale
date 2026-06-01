package com.mycompany.flashsale.simulation.model;

import java.time.LocalDateTime;

public class FlashSaleItem extends BaseEntity {

    private String eventId;
    private String productId;
    private double flashPrice;
    private int limitedQty;
    private int soldQty;
    private int version; // Bắt buộc cho Optimistic Lock của Member B
    private ActivationStatus status; // Tích hợp Enum đồng bộ full hệ thống

    public FlashSaleItem() {
        super();
        this.status = ActivationStatus.ACTIVE;
    }

    public FlashSaleItem(String id, String eventId, String productId, double flashPrice, int limitedQty, int soldQty, int version, ActivationStatus status) {
        super();
        this.setId(id); // Gọi hàm setId kế thừa từ BaseEntity theo chuẩn Leader
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

    public ActivationStatus getStatus() {
        return status;
    }

    public void setStatus(ActivationStatus status) {
        this.status = status;
    }

    @Override
    public String toCsvLine() {
        return String.join(",",
                this.getId(), this.eventId, this.productId, String.valueOf(this.flashPrice),
                String.valueOf(this.limitedQty), String.valueOf(this.soldQty), String.valueOf(this.version),
                this.status.name(),
                this.createdAt.toString(), this.updatedAt.toString()
        );
    }

    public static FlashSaleItem fromCsvLine(String csvLine) {
        String[] parts = csvLine.split(",");
        FlashSaleItem item = new FlashSaleItem();
        item.setId(parts[0]);
        item.setEventId(parts[1]);
        item.setProductId(parts[2]);
        item.setFlashPrice(Double.parseDouble(parts[3]));
        item.setLimitedQty(Integer.parseInt(parts[4]));
        item.setSoldQty(Integer.parseInt(parts[5]));
        item.setVersion(Integer.parseInt(parts[6]));
        item.setStatus(ActivationStatus.valueOf(parts[7]));
        item.setCreatedAt(LocalDateTime.parse(parts[8]));
        item.setUpdatedAt(LocalDateTime.parse(parts[9]));
        return item;
    }
}
