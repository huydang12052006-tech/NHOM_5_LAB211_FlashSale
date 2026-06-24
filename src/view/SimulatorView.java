package view;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

import model.Entity.FlashSaleItem;
import model.Entity.OrderTransaction;
import model.Enum.LockMechanism;

public class SimulatorView {

    private final Scanner scanner;

    public SimulatorView() {
        this.scanner = new Scanner(System.in);
    }

    // ==================================
    // Input Methods
    // ==================================

    public LockMechanism inputLockMechanism() {
        System.out.println("Lock mechanism:");
        System.out.println("1. NO_LOCK");
        System.out.println("2. SYNCHRONIZED");
        System.out.println("3. FILE_LOCK");
        System.out.println("4. OPTIMISTIC_LOCK");
        System.out.print("Choose: ");

        String choice = scanner.nextLine().trim();

        if ("2".equals(choice)) {
            return LockMechanism.SYNCHRONIZED;
        }
        if ("3".equals(choice)) {
            return LockMechanism.FILE_LOCK;
        }
        if ("4".equals(choice)) {
            return LockMechanism.OPTIMISTIC_LOCK;
        }

        return LockMechanism.NO_LOCK;
    }

    public int inputThreadCount() {
        while (true) {
            try {
                System.out.print("Thread count: ");
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    // ==================================
    // Display Methods
    // ==================================

    public void showDashboard(Map<String, Integer> counts, int threadCount, LockMechanism lock) {
        System.out.println("===== SYSTEM DASHBOARD =====");
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
        System.out.println("Thread count: " + threadCount);
        System.out.println("Lock mechanism: " + lock);
    }

    public void showInventoryReport(List<FlashSaleItem> items) {
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

    public void showNegativeStockReport(List<FlashSaleItem> items) {
        System.out.println("===== NEGATIVE STOCK REPORT =====");

        if (items == null || items.isEmpty()) {
            System.out.println("No negative stock items found.");
            return;
        }

        for (FlashSaleItem item : items) {
            System.out.println(item.getId()
                    + " | product=" + item.getProductId()
                    + " | limited=" + item.getLimitedQty()
                    + " | sold=" + item.getSoldQty()
                    + " | status=" + item.getStatus());
        }
    }

    public void showThroughputReport(int total, int success, long totalMs, double tps) {
        System.out.println("===== THROUGHPUT REPORT =====");
        System.out.println("Transactions: " + total);
        System.out.println("Success: " + success);
        System.out.println("Total execution time ms: " + totalMs);
        System.out.println("TPS: " + tps);
    }

    public void showRetryRate(double rate) {
        System.out.println("Retry rate: " + rate);
    }

    public void showNegativeStockRate(double rate) {
        System.out.println("Negative stock rate: " + rate + "%");
    }

    public void showThreadCountConfigured(int count) {
        System.out.println("Configured thread count: " + count);
    }

    public void showSimulationHeader(int threadCount, LockMechanism lock) {
        System.out.println("Thread count: " + threadCount);
        System.out.println("Lock mechanism: " + lock);
    }

    public void showExportResult(boolean success, String path) {
        System.out.println(success
                ? "[SUCCESS] Exported to " + path
                : "[FAILED] Unable to export simulation result.");
    }

    // ==================================
    // Legacy display methods (kept for SimulatorController compatibility)
    // ==================================

    public void displaySimulationResult() {
        System.out.println("===== SIMULATION RESULT =====");
    }

    public void displaySimulationResult(List<OrderTransaction> transactions) {
        displaySimulationResult();

        if (transactions == null || transactions.isEmpty()) {
            System.out.println("No simulation transactions found.");
            return;
        }

        for (OrderTransaction transaction : transactions) {
            System.out.println(transaction);
        }
    }

    public void displayTPSReport() {
        System.out.println("===== TPS REPORT =====");
    }

    public void displayTPSReport(double tps) {
        displayTPSReport();
        System.out.println("TPS: " + tps);
    }

    public void displayRaceConditionResult() {
        System.out.println("===== RACE CONDITION RESULT =====");
    }

    public void displayRaceConditionResult(String message) {
        displayRaceConditionResult();
        System.out.println(message);
    }

    public void displayLockComparison() {
        System.out.println("===== LOCK COMPARISON =====");
    }

    public void displayLockComparison(Map<LockMechanism, Integer> successCountByMechanism) {
        displayLockComparison();

        if (successCountByMechanism == null || successCountByMechanism.isEmpty()) {
            System.out.println("No lock comparison data found.");
            return;
        }

        for (Map.Entry<LockMechanism, Integer> entry : successCountByMechanism.entrySet()) {
            System.out.println(entry.getKey() + " success count: " + entry.getValue());
        }
    }
}
