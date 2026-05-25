package com.mycompany.flashsale.simulation.view;

import com.mycompany.flashsale.simulation.model.Product;
import java.util.List;

public class ProductView {

    public void displayMainMenu() {
        System.out.println("\n==================================================");
        System.out.println("====== QUAN LY SAN PHAM & FLASH SALE (MEMBER A) ======");
        System.out.println("==================================================");
        System.out.println("1. Xem danh sach san pham (Top 10)");
        System.out.println("2. Them san pham moi");
        System.out.println("3. Tao su kien Flash Sale");
        System.out.println("4. Them san pham vao Flash Sale");
        System.out.println("5. Xem cac san pham dang Flash Sale hoat dong");
        System.out.println("0. Thoat chuong trinh");
        System.out.println("--------------------------------------------------");
        System.out.print("Moi ban chon chuc nang (0-5): ");
    }

    // Hàm mới thêm vào
    public void displayProducts(List<Product> products) {
        if (products.isEmpty()) {
            System.out.println("[Thong bao]: Danh sach san pham trong.");
            return;
        }
        System.out.println("\n------------------------------------------------------------------------------------");
        System.out.printf("| %-8s | %-30s | %-12s | %-10s | %-6s |\n", "ID", "Ten san pham", "Danh muc", "Gia", "Kho");
        System.out.println("------------------------------------------------------------------------------------");
        for (Product p : products) {
            System.out.printf("| %-8s | %-30s | %-12s | %-10.2f | %-6d |\n",
                    p.getId(), p.getName(), p.getCategory(), p.getOriginalPrice(), p.getStockQty());
        }
        System.out.println("------------------------------------------------------------------------------------");
    }

    public void displayFlashSaleItems(java.util.List<com.mycompany.flashsale.simulation.model.FlashSaleItem> flashItems, com.mycompany.flashsale.simulation.repository.ProductRepository productRepository) {
        if (flashItems.isEmpty()) {
            System.out.println("[Thong bao]: Khong co san pham nao dang trong chuong trinh Flash Sale.");
            return;
        }
        System.out.println("\n---------------------------------------------------------------------------------------------");
        System.out.printf("| %-10s | %-10s | %-25s | %-12s | %-10s | %-6s |\n", "Item ID", "Event ID", "Ten san pham", "Gia Flash", "Gioi han", "Da ban");
        System.out.println("---------------------------------------------------------------------------------------------");
        for (com.mycompany.flashsale.simulation.model.FlashSaleItem item : flashItems) {
            // Tim ten san pham goc de in ra cho nguoi dung de doc
            String productName = "Unknow Product";
            for (com.mycompany.flashsale.simulation.model.Product p : productRepository.findAll()) {
                if (p.getId().equalsIgnoreCase(item.getProductId())) {
                    productName = p.getName();
                    break;
                }
            }
            System.out.printf("| %-10s | %-10s | %-25s | %-12.2f | %-10d | %-6d |\n",
                    item.getId(), item.getEventId(), productName, item.getFlashPrice(), item.getLimitedQty(), item.getSoldQty());
        }
        System.out.println("---------------------------------------------------------------------------------------------");
    }
}
