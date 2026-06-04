package controller;

import java.time.LocalDateTime;
import model.BaseEntity.BaseEntity;
import model.Enum.SaleStatus;
import repository.ProductRepository;  
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

        boolean idExists = productRepository.findAll().stream().anyMatch(p -> p.getId().equalsIgnoreCase(id));
        if (idExists) {
            System.out.println("[Loi]: ID san pham nay da ton tai trong he thong!");
            return;
        }

        System.out.print("Nhap ten san pham: ");
        String name = scanner.nextLine().trim();

        System.out.print("Nhap danh muc: ");
        String category = scanner.nextLine().trim();

        double price;
        try {
            System.out.print("Nhap gia ban gốc: ");
            price = Double.parseDouble(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("[Loi]: Gia san pham khong hop le!");
            return;
        }

        int stock;
        try {
            System.out.print("Nhap so luong nhap kho: ");
            stock = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("[Loi]: So luong kho phai la so nguyen!");
            return;
        }

        // Tạo mới với trạng thái mặc định là ACTIVE chuẩn Enum hệ thống
        Product newProduct = new Product(id, name, category, price, stock, 1, ActivationStatus.ACTIVE);
        productRepository.save(newProduct);
        System.out.println("[Thong bao]: Them san pham moi vao file CSV thanh cong!");
    }

    // CHỨC NĂNG 6: Xóa mềm sản phẩm bằng cách chuyển trạng thái thành DELETED
    public void removeProduct() {
        System.out.println("\n--- TIEN HANH AN/XOA SAN PHAM ---");
        System.out.print("Nhap ID san pham ban muon xoa: ");
        String id = scanner.nextLine().trim();

        Product targetProduct = null;
        for (Product p : productRepository.findAll()) {
            if (p.getId().equalsIgnoreCase(id)) {
                targetProduct = p;
                break;
            }
        }

        if (targetProduct == null) {
            System.out.println("[Loi]: San pham mang ma ID '" + id + "' khong ton tai!");
            return;
        }

        // Tạo bản ghi cập nhật với trạng thái DELETED đúng chuẩn Enum
        Product updatedProduct = new Product(
                targetProduct.getId(),
                targetProduct.getName(),
                targetProduct.getCategory(),
                targetProduct.getOriginalPrice(),
                targetProduct.getStockQty(),
                targetProduct.getVersion(),
                ActivationStatus.DELETED // Chuyển trạng thái sang DELETED để ẩn khỏi hệ thống
        );

        updatedProduct.setCreatedAt(targetProduct.getCreatedAt());
        updatedProduct.setUpdatedAt(LocalDateTime.now());

        productRepository.save(updatedProduct);
        System.out.println("[Thong bao]: Da xoa (an) san pham '" + id + "' thanh cong khoi he thong.");
    }
}
