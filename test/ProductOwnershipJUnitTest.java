package test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import controller.ProductController;
import java.nio.file.Files;
import java.nio.file.Path;
import model.Entity.Product;
import model.Enum.SaleStatus;
import org.junit.jupiter.api.Test;
import repository.ProductRepository;

class ProductOwnershipJUnitTest {

    @Test
    void sellerOnlySeesProductsLinkedToTheirAccountAndIdIsGenerated() throws Exception {
        Path file = Files.createTempFile("products-owner-", ".csv");
        try {
            Files.write(file, java.util.Collections.singletonList(
                    "id,createdAt,updatedAt,name,category,originalPrice,stockQty,version,status,sellerId"));
            ProductController controller = new ProductController(new ProductRepository(file.toString()));
            Product product = new Product(null, null, null, "Keyboard", "Accessory",
                    500000, 10, 1, SaleStatus.ACTIVE);

            assertTrue(controller.createProduct(product, "U02501"));
            assertEquals("P00001", product.getId());
            assertEquals(1, controller.getProductsBySellerId("U02501").size());
            assertEquals(0, controller.getProductsBySellerId("U02502").size());
        } finally {
            Files.deleteIfExists(file);
        }
    }
}
