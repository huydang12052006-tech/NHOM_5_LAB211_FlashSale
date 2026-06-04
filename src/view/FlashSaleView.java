package view;

public class FlashSaleView {

    // =====================================
    // Display Flash Sale Events
    // =====================================

    public void displayFlashSaleEvents(
            String eventInfo) {

        System.out.println();
        System.out.println(
                "===== FLASH SALE EVENTS ====="
        );

        System.out.println(eventInfo);

        System.out.println(
                "============================="
        );
    }

    // =====================================
    // Display Flash Sale Items
    // =====================================

    public void displayFlashSaleItems(
            String itemInfo) {

        System.out.println();
        System.out.println(
                "===== FLASH SALE ITEMS ====="
        );

        System.out.println(itemInfo);

        System.out.println(
                "============================"
        );
    }

    // =====================================
    // Create Event Success
    // =====================================

    public void showCreateEventSuccess() {

        System.out.println();
        System.out.println(
                "[SUCCESS] Flash Sale event created successfully."
        );
    }

    // =====================================
    // Update Stock Result
    // =====================================

    public void showUpdateStockResult(
            boolean success) {

        if (success) {

            System.out.println(
                    "[SUCCESS] Stock updated successfully."
            );

        } else {

            System.out.println(
                    "[FAILED] Unable to update stock."
            );
        }
    }

    
}