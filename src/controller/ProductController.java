package controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

import model.Entity.Product;
import model.Entity.User;
import repository.ProductRepository;
import repository.UserRepository;

public class ProductController {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
        this.userRepository = new UserRepository();
    }

    // ==================================
    // Read
    // ==================================
    public List<Product> getAllProducts() {
        List<Product> result = new ArrayList<Product>();
        for (Product product : productRepository.findAll()) {
            if (isVisibleForCustomers(product)) {
                result.add(product);
            }
        }
        return result;
    }

    public Product getProductById(String id) {
        Product product = productRepository.findById(id);
        return isVisibleForCustomers(product) ? product : null;
    }

    public List<Product> searchProducts(String keyword) {
        List<Product> result = new ArrayList<Product>();
        for (Product product : productRepository.searchByKeyword(keyword)) {
            if (isVisibleForCustomers(product)) {
                result.add(product);
            }
        }
        return result;
    }

    public List<Product> getProductsBySellerId(String sellerId) {
        List<Product> result = new ArrayList<Product>();
        for (Product product : productRepository.findAll()) {
            if (sellerId != null && sellerId.equalsIgnoreCase(product.getSellerId())) {
                result.add(product);
            }
        }
        return result;
    }

    public Product getAnyProductById(String id) {
        return productRepository.findById(id);
    }

    public boolean isVisibleForCustomers(Product product) {
        if (product == null || product.getStatus() != model.Enum.SaleStatus.ACTIVE) {
            return false;
        }
        User seller = userRepository.findById(product.getSellerId());
        return seller != null && seller.isActive();
    }

    // ==================================
    // Create
    // ==================================
    public boolean createProduct(Product newProduct) {

        if (newProduct.getId() == null || newProduct.getId().trim().isEmpty()) {
            newProduct.setId(productRepository.generateNextId());
        }

        boolean exists = false;

        for (Product p : productRepository.findAll()) {
            if (p.getId().equalsIgnoreCase(newProduct.getId())) {
                exists = true;
                break;
            }
        }
        if (exists) {
            return false;
        }

        newProduct.setCreatedAt(LocalDateTime.now());
        newProduct.setUpdatedAt(LocalDateTime.now());

        productRepository.save(newProduct);

        return true;
    }

    public boolean createProduct(Product newProduct, String sellerId) {
        newProduct.setId(productRepository.generateNextId());
        newProduct.setSellerId(sellerId);
        return createProduct(newProduct);
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
