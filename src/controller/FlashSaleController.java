package controller;

import  model.Enum.SaleStatus;
import  model.Entity.FlashSaleEvent;
import  model.Entity.FlashSaleItem;
import  model.Entity.Product;
import  repository.FlashSaleRepository;
import  repository.FlashSaleItemRepository;
import  repository.ProductRepository;

import java.time.LocalDateTime;
import java.util.Scanner;

public class FlashSaleController {

    private final FlashSaleRepository flashSaleRepository;
    private final FlashSaleItemRepository flashSaleItemRepository;
    private final ProductRepository productRepository;
    private final Scanner scanner;

    public FlashSaleController(FlashSaleRepository flashSaleRepository, FlashSaleItemRepository flashSaleItemRepository, ProductRepository productRepository) {
        this.flashSaleRepository = flashSaleRepository;
        this.flashSaleItemRepository = flashSaleItemRepository;
        this.productRepository = productRepository;
        this.scanner = new Scanner(System.in);
    }

    // Chuc nang 3: Tao event moi
    public void createEvent() {
        System.out.println("\n--- TIEN HANH TAO SU KIEN FLASH SALE MOI ---");
        System.out.print("Nhap ID Event (vi du: E99): ");
        String id = scanner.nextLine().trim();

        System.out.print("Nhap ten su kien (vi du: Sale Giua Dem): ");
        String name = scanner.nextLine().trim();

        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusHours(2);

        FlashSaleEvent newEvent = new FlashSaleEvent(id, LocalDateTime.now(), LocalDateTime.now(),name, start, end, SaleStatus.ACTIVE);
        flashSaleRepository.save(newEvent);
        System.out.println("[Thong bao]: Tao su kien Flash Sale moi thanh cong!");
    }

    // Chuc nang 4: Dua san pham vao event kem kiem tra validation
    public void addProductToEvent() {
        System.out.println("\n--- TIEN HANH THEM SAN PHAM VAO FLASH SALE ---");
        System.out.print("Nhap ID Event: ");
        String eventId = scanner.nextLine().trim();

        // 1. Kiem tra su kien ton tai
        boolean eventExists = false;
        for (FlashSaleEvent e : flashSaleRepository.findAll()) {
            if (e.getId().equalsIgnoreCase(eventId)) {
                eventExists = true;
                break;
            }
        }

        if (!eventExists) {
            System.out.println("[Loi]: Su kien Flash Sale nay khong ton tai!");
            return;
        }

        System.out.print("Nhap ID San pham muon dua vao sale: ");
        String productId = scanner.nextLine().trim();

        // 2. Kiem tra san pham goc ton tai và phải đang ở trạng thái ACTIVE (Yêu cầu nghiệp vụ hệ thống)
        Product product = null;
        for (Product p : productRepository.findAll()) {
            if (p.getId().equalsIgnoreCase(productId)) {
                // Kiểm tra xem sản phẩm có bị xóa mềm (DELETED) hoặc ngưng hoạt động (INACTIVE) chưa
                if (p.getStatus() == SaleStatus.ACTIVE) {
                    product = p;
                }
                break;
            }
        }

        if (product == null) {
            System.out.println("[Loi]: San pham nay khong ton tai hoac da bi xoa khoi kho goc!");
            return;
        }

        System.out.print("Nhap gia Flash Sale: ");
        double flashPrice = Double.parseDouble(scanner.nextLine());

        System.out.print("Nhap so luong gioi han mo ban trong Flash Sale: ");
        int limitedQty = Integer.parseInt(scanner.nextLine());

        // 3. Quy tac nghiep vu: So luong mo ban <= So luong kho goc
        if (limitedQty > product.getStockQty()) {
            System.out.println("[Loi]: So luong mo ban Flash Sale khong duoc lon hon ton kho goc (" + product.getStockQty() + ")!");
            return;
        }

        String itemId = "ITEM" + (System.currentTimeMillis() % 10000);

        // CẬP NHẬT CHUẨN: Truyền thêm tham số thứ 8 là ActivationStatus.ACTIVE theo đúng thiết kế của Leader
        FlashSaleItem item = new FlashSaleItem(itemId, LocalDateTime.now(), LocalDateTime.now(),eventId, productId, flashPrice, limitedQty, 0, 1, SaleStatus.ACTIVE);

        flashSaleItemRepository.update(item);
        System.out.println("[Thong bao]: Dua san pham vao Flash Sale thanh cong!");
    }

}
