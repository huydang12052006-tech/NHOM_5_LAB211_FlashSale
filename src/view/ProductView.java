package com.mycompany.flashsale.simulation.view;

import com.mycompany.flashsale.simulation.model.ActivationStatus;
import com.mycompany.flashsale.simulation.model.Product;
import com.mycompany.flashsale.simulation.repository.ProductRepository;
import com.mycompany.flashsale.simulation.model.FlashSaleItem;
import java.util.List;

public class ProductView {

    public void displayMainMenu() {
        System.out.println("\n=======================================================");
        System.out.println("====== QUAN LY SAN PHAM & FLASH SALE (MEMBER A) ======");
        System.out.println("=======================================================");
        System.out.println("1. Xem danh sach san pham (Top 10)");
        System.out.println("2. Them san pham moi");
        System.out.println("3. Tao su kien Flash Sale");
        System.out.println("4. Them san pham vao Flash Sale");
        System.out.println("5. Xem cac san pham dang Flash Sale hoat dong");
        System.out.println("6. Xoa san pham (An khoi he thong)");
        System.out.println("0. Thoat chuong trinh");
        System.out.println("-------------------------------------------------------");
        System.out.print("Moi ban chon chuc nang (0-6): ");
    }

    // Chức năng 1: Chỉ hiển thị sản phẩm đang ở trạng thái ACTIVE
    public void displayProducts(List<Product> products) {
        if (products.isEmpty()) {
            System.out.println("[Thong bao]: Danh sach san pham trong.");
            return;
        }

        System.out.println("\n-------------------------------------------------------------------------------");
        System.out.printf("| %-8s | %-30s | %-12s | %-10s | %-6s |\n", "ID", "Ten san pham", "Danh muc", "Gia", "Kho");
        System.out.println("-------------------------------------------------------------------------------");

        int count = 0;
        for (Product p : products) {
            // LỌC CHUẨN ENUM: Chỉ hiển thị sản phẩm ACTIVE, bỏ qua sản phẩm DELETED hoặc INACTIVE
            if (p.getStatus() != ActivationStatus.ACTIVE) {
                continue;
            }

            System.out.printf("| %-8s | %-30s | %-12s | %-10.2f | %-6d |\n",
                    p.getId(), p.getName(), p.getCategory(), p.getOriginalPrice(), p.getStockQty());
            count++;
        }

        if (count == 0) {
            System.out.println("|                     Khong co san pham nao kha dung!                         |");
        }
        System.out.println("-------------------------------------------------------------------------------");
    }

    // Chức năng 5: Hiển thị danh sách Flash Sale theo các thuộc tính gốc của nhóm bạn
    public void displayFlashSaleItems(List<FlashSaleItem> flashItems, ProductRepository productRepository) {
        if (flashItems.isEmpty()) {
            System.out.println("[Thong bao]: Danh sach mat hang Flash Sale trong.");
            return;
        }

        System.out.println("\n-------------------------------------------------------------------------------------");
        System.out.printf("| %-10s | %-10s | %-15s | %-15s | %-10s |\n", "Item ID", "Event ID", "Gia Sale", "SL Gioi Han", "Con Lai");
        System.out.println("-------------------------------------------------------------------------------------");

        for (FlashSaleItem item : flashItems) {
            // Chỉ hiển thị các mặt hàng Flash Sale đang ở trạng thái ACTIVE
            if (item.getStatus() != ActivationStatus.ACTIVE) {
                continue;
            }

            int availableQty = item.getLimitedQty() - item.getSoldQty();

            System.out.printf("| %-10s | %-10s | %-15.2f | %-15d | %-10d |\n",
                    item.getProductId(), item.getEventId(), item.getFlashPrice(), item.getLimitedQty(), availableQty);
        }
        System.out.println("-------------------------------------------------------------------------------------");
    }
}
