package controller;

import java.time.LocalDateTime;
import model.Entity.Product;
import model.Enum.SaleStatus;
import repository.ProductRepository;

public class ProductController {

    private final ProductRepository productRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public boolean createProduct(Product newProduct) {

        boolean idExists =
                productRepository.findAll()
                        .stream()
                        .anyMatch(p ->
                                p.getId()
                                 .equalsIgnoreCase(
                                         newProduct.getId()));

        if (idExists) {
            return false;
        }

        newProduct.setCreatedAt(LocalDateTime.now());
        newProduct.setUpdatedAt(LocalDateTime.now());

        productRepository.save(newProduct);

        return true;
    }

    public boolean deleteProduct(String id) {

        Product targetProduct = null;

        for (Product p : productRepository.findAll()) {

            if (p.getId().equalsIgnoreCase(id)) {

                targetProduct = p;

                break;
            }
        }

        if (targetProduct == null) {
            return false;
        }

        targetProduct.setStatus(
                SaleStatus.DISABLED
        );

        targetProduct.setUpdatedAt(
                LocalDateTime.now()
        );

        return productRepository.update(
                targetProduct
        );
    }
}