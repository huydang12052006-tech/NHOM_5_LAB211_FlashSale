package view;

import model.Entity.FlashSaleItem;
import model.Entity.Order;
import model.Entity.OrderDetail;
import model.Enum.PaymentMethod;

import java.util.List;
import java.util.Map;
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
        System.out.println("\nWhat would you like to buy?");
        System.out.println("1. Flash Sale Item");
        System.out.println("2. Regular Product");
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
        displayOrder(order, null);
    }

    public void displayOrder(Order order, String eventName) {
        if (order == null) {
            System.out.println("[INFO] Order not found.");
            return;
        }

        System.out.println("========== ORDER ==========");
        System.out.println("Order ID: " + order.getId());
        System.out.println("Created at: " + order.getCreatedAt());
        System.out.printf("Total: %.0f VND%n", order.getTotalAmount());
        System.out.println("Status: " + order.getStatus());
        System.out.println("Event: " + orderEventName(order, eventName));
        System.out.println("Processing mode: " + order.getLockMechanism());
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

    public void displaySellerOrderReview(List<Order> orders, Map<String, String> productSummaryByOrder) {
        System.out.println("\n===== CUSTOMER ORDERS TO REVIEW =====");

        if (orders == null || orders.isEmpty()) {
            System.out.println("No orders found.");
            return;
        }

        System.out.println("-------------------------------------------------------------------------------------------------------------------------");
        System.out.printf("| %-10s | %-19s | %-14s | %-12s | %-50s |%n",
                "Order ID", "Created at", "Total (VND)", "Status", "Your items");
        System.out.println("-------------------------------------------------------------------------------------------------------------------------");
        for (Order order : orders) {
            String summary = productSummaryByOrder == null ? "" : productSummaryByOrder.get(order.getId());
            if (summary == null || summary.trim().isEmpty()) {
                summary = "No item summary";
            }
            System.out.printf("| %-10s | %-19s | %-14.0f | %-12s | %-50s |%n",
                    order.getId(), order.getCreatedAt(), order.getTotalAmount(),
                    order.getStatus(), fit(summary, 50));
        }
        System.out.println("-------------------------------------------------------------------------------------------------------------------------");
    }

    public void displayBuyerOrderHistory(List<Order> orders) {
        displayBuyerOrderHistory(orders, java.util.Collections.<String, String>emptyMap());
    }

    public void displayBuyerOrderHistory(List<Order> orders, Map<String, String> eventNames) {
        System.out.println("\n===== MY ORDERS =====");
        if (orders == null || orders.isEmpty()) {
            System.out.println("No orders found.");
            return;
        }
        System.out.println("--------------------------------------------------------------------------------------------------");
        System.out.printf("| %-10s | %-19s | %-14s | %-12s | %-28s |%n",
                "Order ID", "Created at", "Total (VND)", "Status", "Event");
        System.out.println("--------------------------------------------------------------------------------------------------");
        for (Order order : orders) {
            System.out.printf("| %-10s | %-19s | %-14.0f | %-12s | %-28s |%n",
                    order.getId(), order.getCreatedAt(), order.getTotalAmount(),
                    order.getStatus(), fit(orderEventName(order, eventNames), 28));
        }
        System.out.println("--------------------------------------------------------------------------------------------------");
    }

    public void displayOrderDetails(List<OrderDetail> details, Map<String, String> productNames,
                                    Map<String, String> sellerNames, Map<String, String> eventNames) {
        System.out.println("\n===== ORDER DETAILS =====");
        if (details == null || details.isEmpty()) {
            System.out.println("No order details found.");
            return;
        }
        System.out.println("---------------------------------------------------------------------------------------------------------------------------------");
        System.out.printf("| %-28s | %-20s | %-24s | %-8s | %-14s | %-14s |%n",
                "Product", "Seller", "Event", "Qty", "Unit (VND)", "Subtotal");
        System.out.println("---------------------------------------------------------------------------------------------------------------------------------");
        for (OrderDetail detail : details) {
            String productName = productNames == null ? null : productNames.get(detail.getId());
            if (productName == null || productName.trim().isEmpty()) {
                productName = "Unknown product";
            }
            String sellerName = sellerNames == null ? null : sellerNames.get(detail.getId());
            if (sellerName == null || sellerName.trim().isEmpty()) {
                sellerName = "Unknown seller";
            }
            String eventName = eventNames == null ? null : eventNames.get(detail.getId());
            if (eventName == null || eventName.trim().isEmpty()) {
                eventName = "Regular Product";
            }
            System.out.printf("| %-28s | %-20s | %-24s | %-8d | %-14.0f | %-14.0f |%n",
                    fit(productName, 28), fit(sellerName, 20), fit(eventName, 24), detail.getQuantity(),
                    detail.getUnitPrice(), detail.getSubTotal());
        }
        System.out.println("---------------------------------------------------------------------------------------------------------------------------------");
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

    private String orderEventName(Order order, Map<String, String> eventNames) {
        if (eventNames == null) {
            return "Regular Product";
        }
        String eventName = eventNames.get(order.getId());
        return eventName == null || eventName.trim().isEmpty() ? "Regular Product" : eventName;
    }

    private String orderEventName(Order order, String eventName) {
        return eventName == null || eventName.trim().isEmpty() ? "Regular Product" : eventName;
    }

    private String fit(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength - 3) + "...";
    }
}
