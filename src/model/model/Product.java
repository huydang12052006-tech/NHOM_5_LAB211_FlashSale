package com.mycompany.flashsale.simulation.model;

import java.time.LocalDateTime;

public class Product extends BaseEntity {

    private String name;
    private String category;
    private double originalPrice;
    private int stockQty;
    private int version; // Bắt buộc cho Optimistic Lock của Member B
    private ActivationStatus status; // Chuẩn Enum full hệ thống thay cho boolean active

    public Product() {
        super();
        this.status = ActivationStatus.ACTIVE; // Mặc định khi tạo mới là ACTIVE
    }

    public Product(String id, String name, String category, double originalPrice, int stockQty, int version, ActivationStatus status) {
        super();
        this.setId(id); // Gọi hàm setId kế thừa theo đúng yêu cầu của Leader
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

    public ActivationStatus getStatus() {
        return status;
    }

    public void setStatus(ActivationStatus status) {
        this.status = status;
    }

    @Override
    public String toCsvLine() {
        return String.join(",",
                this.getId(), this.name, this.category,
                String.valueOf(this.originalPrice), String.valueOf(this.stockQty),
                String.valueOf(this.version), this.status.name(), // Lưu tên Enum vào CSV (ACTIVE/INACTIVE/DELETED)
                this.createdAt.toString(), this.updatedAt.toString()
        );
    }

    public static Product fromCsvLine(String csvLine) {
        String[] parts = csvLine.split(",");
        Product p = new Product();
        p.setId(parts[0]);
        p.setName(parts[1]);
        p.setCategory(parts[2]);
        p.setOriginalPrice(Double.parseDouble(parts[3]));
        p.setStockQty(Integer.parseInt(parts[4]));
        p.setVersion(Integer.parseInt(parts[5]));
        p.setStatus(ActivationStatus.valueOf(parts[6])); // Đọc Enum từ chuỗi CSV
        p.setCreatedAt(LocalDateTime.parse(parts[7]));
        p.setUpdatedAt(LocalDateTime.parse(parts[8]));
        return p;
    }
}
