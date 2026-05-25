package com.mycompany.flashsale.simulation.controller;

import com.mycompany.flashsale.simulation.model.Product;
import com.mycompany.flashsale.simulation.repository.ProductRepository;
import java.util.Scanner;

public class ProductController {

    private final ProductRepository productRepository;
    private final Scanner scanner;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
        this.scanner = new Scanner(System.in);
    }

    public void createNewProduct() {
        System.out.println("\n--- TIEN HANH THEM SAN PHAM MOI ---");

        System.out.print("Nhap ID san pham (vi du: P999): ");
        String id = scanner.nextLine().trim();

        // Kiem tra ID da ton tai chua
        boolean idExists = productRepository.findAll().stream().anyMatch(p -> p.getId().equalsIgnoreCase(id));
        if (idExists) {
            System.out.println("[Loi]: ID san pham nay da ton tai trong he thong!");
            return;
        }

        System.out.print("Nhap ten san pham: ");
        String name = scanner.nextLine().trim();

        System.out.print("Nhap danh muc (Laptop/Smartphone/v.v...): ");
        String category = scanner.nextLine().trim();

        double price;
        try {
            System.out.print("Nhap gia ban gốc: ");
            price = Double.parseDouble(scanner.nextLine());
            if (price <= 0) {
                System.out.println("[Loi]: Gia san pham phai lon hon 0!");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("[Loi]: Gia san pham phai la mot so!");
            return;
        }

        int stock;
        try {
            System.out.print("Nhap so luong nhap kho: ");
            stock = Integer.parseInt(scanner.nextLine());
            if (stock < 0) {
                System.out.println("[Loi]: So luong kho khong duoc am!");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("[Loi]: So luong kho phai la mot so nguyen!");
            return;
        }

        // Tao doi tuong Product moi
        Product newProduct = new Product(id, name, category, price, stock, 1, true);

        // Goi repository luu lai vao file CSV
        productRepository.save(newProduct);
        System.out.println("[Thong bao]: Them san pham moi vao file CSV thanh cong!");
    }
}
