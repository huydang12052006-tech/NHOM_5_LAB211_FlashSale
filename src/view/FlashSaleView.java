package view;

import java.util.List;
import java.util.Scanner;

import model.Entity.FlashSaleEvent;
import model.Entity.FlashSaleItem;
import model.Enum.SaleStatus;

public class FlashSaleView {

    private final Scanner scanner;

    public FlashSaleView() {
        this.scanner = new Scanner(System.in);
    }

    // ==================================
    // Input Methods
    // ==================================

    public String inputEventId() {
        System.out.print("Event ID: ");
        return scanner.nextLine().trim();
    }

    public String inputNewEventName() {
        System.out.print("New event name: ");
        return scanner.nextLine().trim();
    }

    /**
     * Inputs basic event data (id + name) for creation.
     * Returns String[2]: [id, name]
     */
    public String[] inputNewEvent() {
        System.out.print("Event ID: ");
        String id = scanner.nextLine().trim();
        System.out.print("Event name: ");
        String name = scanner.nextLine().trim();
        return new String[]{id, name};
    }

    public String inputFlashItemId() {
        System.out.print("Flash item ID: ");
        return scanner.nextLine().trim();
    }

    public SaleStatus inputEventStatus() {
        System.out.println("Status: 1=UPCOMING  2=ACTIVE  3=ENDED");
        System.out.print("Choose: ");
        String choice = scanner.nextLine().trim();
        if ("2".equals(choice)) return SaleStatus.ACTIVE;
        if ("3".equals(choice)) return SaleStatus.ENDED;
        return SaleStatus.UPCOMING;
    }

    // ==================================
    // Display Methods
    // ==================================

    public void displayEventSelection(List<FlashSaleEvent> events) {
        System.out.println("\n===== AVAILABLE FLASH SALE EVENTS =====");

        if (events == null || events.isEmpty()) {
            System.out.println("No events available.");
            return;
        }

        System.out.println("-------------------------------------------------------------------------------------------------");
        System.out.printf("| %-3s | %-10s | %-30s | %-20s | %-20s |\n", "No", "Event ID", "Event Name", "Start Time", "End Time");
        System.out.println("-------------------------------------------------------------------------------------------------");

        int index = 1;
        for (FlashSaleEvent event : events) {
            System.out.printf("| %-3d | %-10s | %-30s | %-20s | %-20s |\n",
                    index++,
                    event.getId(),
                    event.getEventName(),
                    event.getStartTime().toString(),
                    event.getEndTime().toString());
        }
        System.out.println("-------------------------------------------------------------------------------------------------");
    }

    public void displayActiveEvents(List<FlashSaleEvent> events) {
        System.out.println("===== ACTIVE FLASH SALE EVENTS =====");

        if (events == null || events.isEmpty()) {
            System.out.println("No active events found.");
            return;
        }

        System.out.println("-------------------------------------------------------------------------------------------------");
        System.out.printf("| %-10s | %-30s | %-20s | %-20s |\n", "Event ID", "Event Name", "Start Time", "End Time");
        System.out.println("-------------------------------------------------------------------------------------------------");
        for (FlashSaleEvent event : events) {
            System.out.printf("| %-10s | %-30s | %-20s | %-20s |\n",
                    event.getId(),
                    event.getEventName(),
                    event.getStartTime().toString(),
                    event.getEndTime().toString());
        }
        System.out.println("-------------------------------------------------------------------------------------------------");
    }



    public void displayFlashSaleItemsInTable(List<FlashSaleItem> items, controller.ProductController pc) {
        System.out.println("===== FLASH SALE ITEMS IN EVENT =====");

        if (items == null || items.isEmpty()) {
            System.out.println("No items in this event.");
            return;
        }

        System.out.println("---------------------------------------------------------------------------------------------------------------");
        System.out.printf("| %-12s | %-35s | %-12s | %-12s | %-11s | %-13s |\n", 
                "Flash Item ID", "Product Name", "Orig. Price", "Flash Price", "Discount %", "Remaining Qty");
        System.out.println("---------------------------------------------------------------------------------------------------------------");
        for (FlashSaleItem item : items) {
            model.Entity.Product product = pc.getProductById(item.getProductId());
            String prodName = (product != null) ? product.getName() : "Unknown Product";
            double origPrice = (product != null) ? product.getOriginalPrice() : 0.0;
            int remaining = item.getLimitedQty() - item.getSoldQty();
            
            System.out.printf("| %-12s | %-35s | %-12.0f | %-12.0f | %-10.1f%s | %-13d |\n", 
                    item.getId(), 
                    prodName.length() > 35 ? prodName.substring(0, 32) + "..." : prodName, 
                    origPrice, 
                    item.getFlashPrice(), 
                    item.getDiscountPercent(), "%",
                    remaining);
        }
        System.out.println("---------------------------------------------------------------------------------------------------------------");
    }

    public void displayEventList(List<FlashSaleEvent> events) {
        System.out.println("===== FLASH SALE EVENTS =====");

        if (events == null || events.isEmpty()) {
            System.out.println("No events found.");
            return;
        }

        for (FlashSaleEvent event : events) {
            System.out.println(formatEvent(event));
        }
    }

    public void displayFlashItem(FlashSaleItem item) {
        if (item == null) {
            System.out.println("[FAILED] Flash sale item not found.");
            return;
        }

        int remaining = item.getLimitedQty() - item.getSoldQty();
        System.out.println(formatFlashItem(item));
        System.out.println("Remaining quantity: " + remaining);
    }

    public void displayInventoryReport(List<FlashSaleItem> items) {
        System.out.println("===== INVENTORY REPORT =====");

        if (items == null || items.isEmpty()) {
            System.out.println("No inventory data found.");
            return;
        }

        for (FlashSaleItem item : items) {
            int remaining = item.getLimitedQty() - item.getSoldQty();
            System.out.println(item.getId()
                    + " | product=" + item.getProductId()
                    + " | limited=" + item.getLimitedQty()
                    + " | sold=" + item.getSoldQty()
                    + " | remaining=" + remaining
                    + " | status=" + item.getStatus());
        }
    }

    public void displayNegativeStockReport(List<FlashSaleItem> items) {
        System.out.println("===== NEGATIVE STOCK REPORT =====");

        if (items == null || items.isEmpty()) {
            System.out.println("No negative stock items found.");
            return;
        }

        for (FlashSaleItem item : items) {
            System.out.println(formatFlashItem(item));
        }
    }

    public void displayFlashSaleEvents(String eventInfo) {
        System.out.println();
        System.out.println("===== FLASH SALE EVENTS =====");
        System.out.println(eventInfo);
        System.out.println("=============================");
    }

    public void displayFlashSaleItems(String itemInfo) {
        System.out.println();
        System.out.println("===== FLASH SALE ITEMS =====");
        System.out.println(itemInfo);
        System.out.println("============================");
    }

    // ==================================
    // Result Messages
    // ==================================

    public void showCreateEventResult(boolean success) {
        System.out.println(success
                ? "[SUCCESS] Flash event created."
                : "[FAILED] Unable to create event.");
    }

    public void showUpdateEventResult(boolean success) {
        System.out.println(success
                ? "[SUCCESS] Event status updated."
                : "[FAILED] Unable to update event.");
    }

    public void showUpdateEventNameResult(boolean success) {
        System.out.println(success
                ? "[SUCCESS] Event information updated."
                : "[FAILED] Unable to update event.");
    }

    public void showAssignProductResult(boolean success, FlashSaleItem item) {
        if (success) {
            System.out.println("[SUCCESS] Product assigned to event.");
            if (item != null) {
                System.out.println(formatFlashItem(item));
            }
        } else {
            System.out.println("[FAILED] Unable to assign product to event.");
        }
    }

    public void showEventNotFound() {
        System.out.println("[FAILED] Event not found.");
    }

    public void showProductNotFound() {
        System.out.println("[FAILED] Product not found.");
    }

    public void showExceedStockError(int stockQty) {
        System.out.println("[FAILED] Limited quantity cannot exceed product stock (" + stockQty + ").");
    }

    public void showCreateEventSuccess() {
        System.out.println();
        System.out.println("[SUCCESS] Flash Sale event created successfully.");
    }

    public void showUpdateStockResult(boolean success) {
        if (success) {
            System.out.println("[SUCCESS] Stock updated successfully.");
        } else {
            System.out.println("[FAILED] Unable to update stock.");
        }
    }

    // ==================================
    // Format
    // ==================================

    public String formatFlashItem(FlashSaleItem item) {
        return item.getId()
                + " | event=" + item.getEventId()
                + " | product=" + item.getProductId()
                + " | flashPrice=" + item.getFlashPrice()
                + " | limited=" + item.getLimitedQty()
                + " | sold=" + item.getSoldQty()
                + " | discount=" + item.getDiscountPercent()
                + "%"
                + " | status=" + item.getStatus();
    }

    private String formatEvent(FlashSaleEvent event) {
        return event.getId()
                + " | " + event.getEventName()
                + " | start=" + event.getStartTime()
                + " | end=" + event.getEndTime()
                + " | status=" + event.getStatus();
    }
}