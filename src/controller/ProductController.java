package controller;

import java.io.IOException;
import java.time.LocalDateTime;
import model.Entity.Product;
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

        if (id == null || id.trim().isEmpty()) {
            return false;
        }

        try {
            return productRepository.delete(id.trim());
        } catch (IOException e) {
            System.out.println("[ERROR] " + e.getMessage());
            return false;
        }
    }
}
