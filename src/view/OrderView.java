package view;

import model.Entity.Order;

import java.util.List;

public class OrderView {

    public void displayOrder(Order order) {

        if (order == null) {

            System.out.println(
                    "[INFO] Order not found."
            );

            return;
        }

        System.out.println("========== ORDER ==========");

        System.out.println(order);

        System.out.println("===========================");
    }

    public void displayOrderHistory(
            List<Order> orders) {

        System.out.println(
                "\n===== ORDER HISTORY ====="
        );

        if (orders == null || orders.isEmpty()) {

            System.out.println(
                    "No orders found."
            );

            return;
        }

        for (Order order : orders) {

            System.out.println(order);
        }

        System.out.println(
                "========================="
        );
    }

    public void showOrderSuccess() {

        System.out.println(
                "[SUCCESS] Order placed successfully."
        );
    }

    public void showOrderFailure() {

        System.out.println(
                "[FAILED] Order placement failed."
        );
    }
}