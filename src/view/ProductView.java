package view;

import model.Entity.Product;
import model.Enum.SaleStatus;

import java.util.List;
import java.util.Scanner;

public class ProductView {

    private final Scanner scanner =
            new Scanner(System.in);

    // ==================================
    // Display Products
    // ==================================

    public void displayProducts(
            List<Product> products) {

        System.out.println(
                "\n===== PRODUCT LIST ====="
        );

        if (products == null
                || products.isEmpty()) {

            System.out.println(
                    "No products found."
            );

            return;
        }

        for (Product product : products) {

            System.out.println(product);
        }
    }

    // ==================================
    // Display Product Detail
    // ==================================

    public void displayProductDetail(
            Product product) {

        if (product == null) {

            System.out.println(
                    "Product not found."
            );

            return;
        }

        System.out.println(
                "\n===== PRODUCT DETAIL ====="
        );

        System.out.println(product.toString());
    }

    // ==================================
    // Input Product Data
    // ==================================

    public Product inputProductData() {

        System.out.print("ID: ");
        String id = scanner.nextLine();

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
                id,
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

    // ==================================
    // Input Category
    // ==================================

    public String inputCategory() {

        System.out.print(
                "Enter category: "
        );

        return scanner.nextLine()
                .trim();
    }

    // ==================================
    // Message
    // ==================================

    public String inputProductId() {

        System.out.print(
                "Nhap ID san pham: "
        );

        return scanner.nextLine();
    }


}