package view;

import model.Entity.Product;
import model.Enum.SaleStatus;

import java.util.List;
import java.util.Scanner;

public class ProductView {

    private final Scanner scanner;

    public ProductView() {
        this.scanner = new Scanner(System.in);
    }

    // ==================================
    // Input Methods
    // ==================================

    public Product inputProductData() {

        System.out.print("Name: ");
        String name = scanner.nextLine();

        System.out.print("Category: ");
        String category = scanner.nextLine();

        System.out.print("Price: ");
        double price =
                Double.parseDouble(
                        scanner.nextLine()
                );

        System.out.print("Stock: ");
        int stock =
                Integer.parseInt(
                        scanner.nextLine()
                );

        return new Product(
                null,
                null,
                null,
                name,
                category,
                price,
                stock,
                1,
                SaleStatus.ACTIVE
        );
    }

    public String inputProductId() {
        System.out.print("Product ID: ");
        return scanner.nextLine().trim();
    }

    public String inputKeyword() {
        System.out.print("Keyword/category: ");
        return scanner.nextLine().trim().toLowerCase();
    }

    public String inputNewName() {
        System.out.print("New name: ");
        return scanner.nextLine().trim();
    }

    public String inputNewCategory() {
        System.out.print("New category: ");
        return scanner.nextLine().trim();
    }

    public double inputNewPrice() {
        while (true) {
            try {
                System.out.print("New price: ");
                return Double.parseDouble(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    public String inputCategory() {
        System.out.print("Enter category: ");
        return scanner.nextLine().trim();
    }

    // ==================================
    // Display Methods
    // ==================================

    public void displayProducts(List<Product> products) {
        System.out.println("===== PRODUCTS =====");

        if (products == null || products.isEmpty()) {
            System.out.println("No products found.");
            return;
        }

        System.out.println("-------------------------------------------------------------------------------------");
        System.out.printf("| %-10s | %-40s | %-12s | %-9s |\n", "ID", "Product Name", "Price (VND)", "Stock Qty");
        System.out.println("-------------------------------------------------------------------------------------");
        for (Product product : products) {
            System.out.printf("| %-10s | %-40s | %-12.0f | %-9d |\n", 
                    product.getId(), 
                    truncateName(product.getName(), 40), 
                    product.getOriginalPrice(), 
                    product.getStockQty());
        }
        System.out.println("-------------------------------------------------------------------------------------");
    }

    public void displaySearchResults(List<Product> products) {
        System.out.println("===== SEARCH RESULT =====");

        if (products == null || products.isEmpty()) {
            System.out.println("No products found.");
            return;
        }

        System.out.println("-------------------------------------------------------------------------------------");
        System.out.printf("| %-10s | %-40s | %-12s | %-9s |\n", "ID", "Product Name", "Price (VND)", "Stock Qty");
        System.out.println("-------------------------------------------------------------------------------------");
        for (Product product : products) {
            System.out.printf("| %-10s | %-40s | %-12.0f | %-9d |\n", 
                    product.getId(), 
                    truncateName(product.getName(), 40), 
                    product.getOriginalPrice(), 
                    product.getStockQty());
        }
        System.out.println("-------------------------------------------------------------------------------------");
    }

    private String truncateName(String name, int length) {
        if (name == null) return "";
        if (name.length() <= length) return name;
        return name.substring(0, length - 3) + "...";
    }

    public void displayProductDetail(Product product) {

        if (product == null) {
            System.out.println("Product not found.");
            return;
        }

        System.out.println("\n===== PRODUCT DETAIL =====");
        System.out.println(product.toString());
    }

    // ==================================
    // Result Messages
    // ==================================

    public void showProductNotFound() {
        System.out.println("[FAILED] Product not found.");
    }

    public void showAddProductResult(boolean success) {
        System.out.println(success
                ? "[SUCCESS] Product added."
                : "[FAILED] Product ID exists.");
    }

    public void showUpdateProductResult(boolean success) {
        System.out.println(success
                ? "[SUCCESS] Product updated."
                : "[FAILED] Product not found.");
    }

    public void showDeleteProductResult(boolean success) {
        System.out.println(success
                ? "[SUCCESS] Product deleted."
                : "[FAILED] Product not found.");
    }

    public void showEditInfoResult(boolean success) {
        System.out.println(success
                ? "[SUCCESS] Product information updated."
                : "[FAILED] Unable to update product.");
    }

    public void showEditPriceResult(boolean success) {
        System.out.println(success
                ? "[SUCCESS] Product price updated."
                : "[FAILED] Unable to update product price.");
    }

    // ==================================
    // Format
    // ==================================

    private String formatProduct(Product product) {
        return product.getId()
                + " | " + product.getName()
                + " | category=" + product.getCategory()
                + " | price=" + product.getOriginalPrice()
                + " | stock=" + product.getStockQty()
                + " | status=" + product.getStatus();
    }
}
