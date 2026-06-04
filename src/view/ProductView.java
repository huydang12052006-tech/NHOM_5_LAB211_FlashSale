package view;

import java.util.Scanner;
import model.Entity.Product;
import model.Enum.SaleStatus;

public class ProductView {

    private final Scanner scanner =
            new Scanner(System.in);

    public Product inputProduct() {

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

    public String inputProductId() {

        System.out.print(
                "Nhap ID san pham: "
        );

        return scanner.nextLine();
    }

    public void showMessage(
            String message) {

        System.out.println(message);
    }
}