package controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import model.Entity.Product;
import repository.ProductRepository;

public class ProductController {

    private final ProductRepository productRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // ==================================
    // Read
    // ==================================

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(String id) {
        return productRepository.findById(id);
    }

    public List<Product> searchProducts(String keyword) {
        return productRepository.searchByKeyword(keyword);
    }

    // ==================================
    // Create
    // ==================================

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

    // ==================================
    // Update
    // ==================================

    public boolean updateProduct(Product product) {
        product.setUpdatedAt(LocalDateTime.now());
        return productRepository.update(product);
    }

    public boolean updateProductInfo(String id, String name, String category) {
        Product product = productRepository.findById(id);

        if (product == null) {
            return false;
        }

        product.setName(name);
        product.setCategory(category);
        product.setUpdatedAt(LocalDateTime.now());

        return productRepository.update(product);
    }

    public boolean updateProductPrice(String id, double price) {
        Product product = productRepository.findById(id);

        if (product == null) {
            return false;
        }

        product.setOriginalPrice(price);
        product.setUpdatedAt(LocalDateTime.now());

        return productRepository.update(product);
    }

    // ==================================
    // Delete
    // ==================================

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
