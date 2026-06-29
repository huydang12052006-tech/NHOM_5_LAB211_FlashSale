package test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import controller.FlashSaleController;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import model.Entity.FlashSaleEvent;
import model.Entity.FlashSaleItem;
import model.Entity.Product;
import model.Enum.SaleStatus;
import org.junit.jupiter.api.Test;
import repository.FlashSaleItemRepository;
import repository.FlashSaleRepository;
import repository.ProductRepository;

class FlashSaleFilterJUnitTest {

    @Test
    void customerSeesOnlyActiveAndInStockFlashSaleProducts() throws Exception {
        Path eventFile = Files.createTempFile("events-", ".csv");
        Path itemFile = Files.createTempFile("items-", ".csv");
        Path productFile = Files.createTempFile("products-", ".csv");
        try {
            LocalDateTime now = LocalDateTime.now();
            FlashSaleRepository events = new FlashSaleRepository(eventFile.toString());
            FlashSaleItemRepository items = new FlashSaleItemRepository(itemFile.toString());
            ProductRepository products = new ProductRepository(productFile.toString());
            events.save(new FlashSaleEvent("E001", now, now, "Test", now, now.plusHours(1), SaleStatus.ACTIVE));
            products.save(new Product("P00001", now, now, "Active", "Test", 100, 10, 1, SaleStatus.ACTIVE, "U02501"));
            products.save(new Product("P00002", now, now, "Disabled", "Test", 100, 10, 1, SaleStatus.DISABLED, "U02501"));
            items.save(new FlashSaleItem("FI00001", now, now, "E001", "P00001", 80, 5, 1, 20, 1, SaleStatus.ACTIVE));
            items.save(new FlashSaleItem("FI00002", now, now, "E001", "P00001", 80, 5, 1, 20, 1, SaleStatus.ENDED));
            items.save(new FlashSaleItem("FI00003", now, now, "E001", "P00002", 80, 5, 1, 20, 1, SaleStatus.ACTIVE));

            FlashSaleController controller = new FlashSaleController(events, items, products);
            List<FlashSaleItem> visible = controller.getActiveFlashItemsByEventId("E001");

            assertEquals(1, visible.size());
            assertEquals("FI00001", visible.get(0).getId());
        } finally {
            Files.deleteIfExists(eventFile);
            Files.deleteIfExists(itemFile);
            Files.deleteIfExists(productFile);
        }
    }
}
