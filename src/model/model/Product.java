package com.mycompany.flashsale.simulation.model;

import java.time.LocalDateTime;

public class Product extends BaseEntity {

    private String name;
    private String category;
    private double originalPrice;
    private int stockQty;
    private int version; // Bắt buộc để Member B làm cơ chế khóa Optimistic Lock
    private boolean active;

    public Product() {
        super();
    }

    public Product(String id, String name, String category, double originalPrice, int stockQty, int version, boolean active) {
        super();
        this.id = id;
        this.name = name;
        this.category = category;
        this.originalPrice = originalPrice;
        this.stockQty = stockQty;
        this.version = version;
        this.active = active;
    }

    // Các hàm Getter và Setter cho các thuộc tính riêng của Product...
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toCsvLine() {
        return String.join(",",
                this.id, this.name, this.category,
                String.valueOf(this.originalPrice), String.valueOf(this.stockQty),
                String.valueOf(this.version), String.valueOf(this.active),
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
        p.setActive(Boolean.parseBoolean(parts[6]));
        p.setCreatedAt(LocalDateTime.parse(parts[7]));
        p.setUpdatedAt(LocalDateTime.parse(parts[8]));
        return p;
    }
}
