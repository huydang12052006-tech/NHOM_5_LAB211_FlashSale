package view;

import model.Entity.FlashSaleItem;
import model.Entity.Order;
import model.Enum.PaymentMethod;

import java.util.List;
import java.util.Scanner;

public class OrderView {

    private final Scanner scanner;

    public OrderView() {
        this.scanner = new Scanner(System.in);
    }

    // ==================================
    // Input Methods
    // ==================================

    public String inputOrderId() {
        System.out.print("Order ID: ");
        return scanner.nextLine().trim();
    }

    public String inputCustomerId() {
        System.out.print("Customer ID: ");
        return scanner.nextLine().trim();
    }

    public String inputFlashItemId() {
        System.out.print("Flash item ID: ");
        return scanner.nextLine().trim();
    }

    public String inputProductId() {
        System.out.print("Product ID: ");
        return scanner.nextLine().trim();
    }

    public int inputQuantity() {
        System.out.print("Quantity: ");
        return Integer.parseInt(scanner.nextLine().trim());
    }

    public int inputOrderType() {
        System.out.println("\nSelect order type:");
        System.out.println("1. Flash sale product");
        System.out.println("2. Regular product");
        System.out.print("Choose: ");
        String choice = scanner.nextLine().trim();
        return "2".equals(choice) ? 2 : 1;
    }

    public PaymentMethod inputPaymentMethod() {
        System.out.println("Payment method:");
        System.out.println("1. CASH");
        System.out.println("2. BANKING");
        System.out.print("Choose: ");
        String choice = scanner.nextLine().trim();
        return "2".equals(choice) ? PaymentMethod.BANKING : PaymentMethod.CASH;
    }

    // ==================================
    // Display Methods
    // ==================================

    public void displayOrder(Order order) {

        if (order == null) {
            System.out.println("[INFO] Order not found.");
            return;
        }

        System.out.println("========== ORDER ==========");
        System.out.println(order);
        System.out.println("===========================");
    }

    public void displayOrderHistory(List<Order> orders) {
        System.out.println("\n===== ORDER HISTORY =====");

        if (orders == null || orders.isEmpty()) {
            System.out.println("No orders found.");
            return;
        }

        for (Order order : orders) {
            System.out.println(order);
        }

        System.out.println("=========================");
    }

    public void displayBuyerOrderHistory(List<Order> orders) {
        System.out.println("\n===== MY ORDERS =====");
        if (orders == null || orders.isEmpty()) {
            System.out.println("No orders found.");
            return;
        }
        System.out.println("--------------------------------------------------------------------------------");
        System.out.printf("| %-10s | %-19s | %-14s | %-12s | %-10s |%n",
                "Order ID", "Created at", "Total (VND)", "Status", "Event");
        System.out.println("--------------------------------------------------------------------------------");
        for (Order order : orders) {
            System.out.printf("| %-10s | %-19s | %-14.0f | %-12s | %-10s |%n",
                    order.getId(), order.getCreatedAt(), order.getTotalAmount(),
                    order.getStatus(), order.getEventId());
        }
        System.out.println("--------------------------------------------------------------------------------");
    }

    public void displayInventoryResult(FlashSaleItem item) {
        if (item == null) {
            System.out.println("[FAILED] Flash sale item not found.");
            return;
        }

        int remaining = item.getLimitedQty() - item.getSoldQty();
        System.out.println(item.getId()
                + " | product=" + item.getProductId()
                + " | limited=" + item.getLimitedQty()
                + " | sold=" + item.getSoldQty()
                + " | status=" + item.getStatus());
        System.out.println("Remaining quantity: " + remaining);
    }

    public void displayPurchaseLimitResult(int purchasedQty) {
        System.out.println("Purchased quantity: " + purchasedQty);
        System.out.println(purchasedQty < 2
                ? "[OK] Customer can continue buying this flash item."
                : "[FAILED] Purchase limit reached.");
    }

    // ==================================
    // Result Messages
    // ==================================

    public void showOrderSuccess() {
        System.out.println("[SUCCESS] Order placed successfully.");
    }

    public void showOrderFailure() {
        System.out.println("[FAILED] Order placement failed.");
    }

    public void showOrderNotFound() {
        System.out.println("[FAILED] Order not found.");
    }

    public void showConfirmOrderResult(boolean success) {
        System.out.println(success
                ? "[SUCCESS] Order confirmed."
                : "[FAILED] Unable to confirm order.");
    }

    public void showPaymentSaved() {
        System.out.println("[SUCCESS] Payment saved.");
    }

    public void showPlaceOrderError(String message) {
        System.out.println("[FAILED] " + message);
    }
}
