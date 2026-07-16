package view;

import controller.SimulatorController.SimulationSummary;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

import model.Entity.FlashSaleItem;
import model.Entity.OrderTransaction;
import model.Enum.LockMechanism;

public class SimulatorView {

    private final Scanner scanner;

    public SimulatorView() {
        this(new Scanner(System.in));
    }

    public SimulatorView(Scanner scanner) {
        this.scanner = scanner;
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
                int threadCount = Integer.parseInt(scanner.nextLine().trim());
                
                return threadCount;
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
        System.out.println("Configured lock mechanism: " + lock);
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

    public void showThroughputReport(int total, int rawSuccess, int validSuccess, long totalMs, double tps) {
        System.out.println("===== THROUGHPUT REPORT =====");
        System.out.println("Transactions: " + total);
        System.out.println("Raw success: " + rawSuccess);
        System.out.println("Valid success for TPS: " + validSuccess);
        System.out.println("Elapsed wall-clock time ms: " + totalMs);
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

    public void showLockMechanismConfigured(LockMechanism lock) {
        System.out.println("Configured lock mechanism: " + lock);
    }

    public void showSimulationHeader(int threadCount, LockMechanism lock) {
        System.out.println("Thread count: " + threadCount);
        System.out.println("Configured lock mechanism: " + lock);
        System.out.println("Lock mechanisms: NO_LOCK, SYNCHRONIZED, FILE_LOCK, OPTIMISTIC_LOCK");
    }

    public void showBenchmarkDashboard(int threadCount,
                                       int scenarioCount,
                                       int initialStock,
                                       int orderCount,
                                       int repeatRuns) {
        System.out.println("==============================================================");
        System.out.println("          FLASH SALE CONCURRENCY BENCHMARK");
        System.out.println("==============================================================");
        System.out.println();
        System.out.printf(Locale.US, "%-15s: %d%n", "Threads", threadCount);
        System.out.printf(Locale.US, "%-15s: %s%n", "Inventory", "IN_MEMORY");
        System.out.printf(Locale.US, "%-15s: %d per product%n", "Initial Stock", initialStock);
        System.out.printf(Locale.US, "%-15s: %d%n", "Orders", orderCount);
        System.out.printf(Locale.US, "%-15s: %d runs%n", "Repeat", repeatRuns);
        System.out.println();
        System.out.println("Mechanisms:");
        System.out.println(" - NO_LOCK");
        System.out.println(" - SYNCHRONIZED");
        System.out.println(" - FILE_LOCK");
        System.out.println(" - OPTIMISTIC_LOCK");
        System.out.println();
        System.out.println("==============================================================");
        System.out.println();
    }

    public void showBenchmarkProgressStart(LockMechanism mechanism) {
        System.out.printf(Locale.US, "Running %-20s", mechanism.name());
    }

    public void showBenchmarkProgressDone() {
        System.out.println("Done");
        System.out.println();
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

    public void displayLockComparison(List<SimulationSummary> summaries) {
        displayLockComparison();

        if (summaries == null || summaries.isEmpty()) {
            System.out.println("No lock comparison data found.");
            return;
        }

        String border = "==========================================================================================================================";
        System.out.println(border);
        System.out.printf(Locale.US, "%-16s %7s %7s %6s %8s %8s %8s %14s %8s%n",
                "Mechanism", "Success", "Failed", "Retry", "Negative", "TPS", "Avg(ms)", "vs Baseline", "Goal");
        System.out.println(border);

        for (SimulationSummary summary : summaries) {
            double baselineTps = findBaselineTps(summaries, summary.getScenarioName());
            System.out.printf(Locale.US, "%-16s %7d %7d %6d %8d %8.0f %8.0f %14s %8s%n",
                    displayMechanism(summary.getMechanism()),
                    summary.getSuccessCount(),
                    summary.getFailureCount(),
                    summary.getRetryCount(),
                    summary.getNegativeCount(),
                    summary.getValidTps(),
                    summary.getAverageExecutionTimeMs(),
                    formatVsBaseline(summary, baselineTps),
                    finalGoal(summary, baselineTps));
        }

        System.out.println(border);
        System.out.println("TPS = valid success / elapsed wall-clock time. Baseline = NO_LOCK.");
    }

    public void displayResearchQuestionEvaluation(List<SimulationSummary> summaries) {
        if (summaries == null || summaries.isEmpty()) {
            return;
        }

        System.out.println();
        System.out.println("====================================================");
        System.out.println("Research Question Evaluation");
        System.out.println("====================================================");
        System.out.println();
        System.out.println("Requirement 1");
        System.out.println();
        System.out.println("Negative Stock = 0");
        System.out.println();
        for (SimulationSummary summary : summaries) {
            System.out.printf(Locale.US, "%-16s %s%n",
                    displayMechanism(summary.getMechanism()),
                    summary.getNegativeCount() == 0 ? "PASS" : "FAIL");
        }

        System.out.println();
        System.out.println("----------------------------------------------------");
        System.out.println();
        System.out.println("Requirement 2");
        System.out.println();
        System.out.println("TPS Loss <=30%");
        System.out.println();
        for (SimulationSummary summary : summaries) {
            double baselineTps = findBaselineTps(summaries, summary.getScenarioName());
            System.out.printf(Locale.US, "%-16s %s%n",
                    displayMechanism(summary.getMechanism()),
                    tpsLossPass(summary, baselineTps) ? "PASS" : "FAIL");
        }

        System.out.println();
        System.out.println("----------------------------------------------------");
        System.out.println();
        System.out.println("Final Result");
        System.out.println();
        for (SimulationSummary summary : summaries) {
            double baselineTps = findBaselineTps(summaries, summary.getScenarioName());
            System.out.printf(Locale.US, "%-16s %s%n",
                    displayMechanism(summary.getMechanism()),
                    finalGoal(summary, baselineTps));
        }

        System.out.println();
        System.out.println("Best Mechanism");
        System.out.println();
        System.out.println(bestMechanism(summaries));
    }

    private String formatVsBaseline(SimulationSummary summary, double baselineTps) {
        if (summary.getMechanism() == LockMechanism.NO_LOCK) {
            return "0%";
        }
        if (baselineTps <= 0.0) {
            return "n/a";
        }

        double lossPercent = (baselineTps - summary.getValidTps()) * 100.0 / baselineTps;
        return String.format(Locale.US, "%.1f%%", -lossPercent);
    }

    private double findBaselineTps(List<SimulationSummary> summaries, String scenarioName) {
        for (SimulationSummary summary : summaries) {
            if (summary.getMechanism() == LockMechanism.NO_LOCK
                    && summary.getScenarioName().equals(scenarioName)) {
                return summary.getValidTps();
            }
        }
        return 0.0;
    }

    private boolean tpsLossPass(SimulationSummary summary, double baselineTps) {
        if (summary.getMechanism() == LockMechanism.NO_LOCK) {
            return true;
        }
        if (baselineTps <= 0.0) {
            return false;
        }

        double lossPercent = (baselineTps - summary.getValidTps()) * 100.0 / baselineTps;
        return lossPercent <= 30.0;
    }

    private String finalGoal(SimulationSummary summary, double baselineTps) {
        return summary.getNegativeCount() == 0 && tpsLossPass(summary, baselineTps)
                ? "PASS"
                : "FAIL";
    }

    private String bestMechanism(List<SimulationSummary> summaries) {
        StringBuilder result = new StringBuilder();
        java.util.LinkedHashSet<String> scenarioNames = new java.util.LinkedHashSet<String>();

        for (SimulationSummary summary : summaries) {
            scenarioNames.add(summary.getScenarioName());
        }

        for (String scenarioName : scenarioNames) {
            double baselineTps = findBaselineTps(summaries, scenarioName);
            SimulationSummary best = null;

            for (SimulationSummary summary : summaries) {
                if (summary.getMechanism() == LockMechanism.NO_LOCK
                        || !"PASS".equals(finalGoal(summary, baselineTps))) {
                    continue;
                }
                if (best == null || summary.getValidTps() > best.getValidTps()) {
                    best = summary;
                }
            }

            if (result.length() > 0) {
                result.append(System.lineSeparator());
            }
            result.append(scenarioName).append(": ");
            if (best == null) {
                result.append("No mechanism meets both requirements.");
                continue;
            }
            result.append(best.getMechanism().name());
        }

        return result.toString();
    }

    private String displayMechanism(LockMechanism mechanism) {
        if (mechanism == LockMechanism.OPTIMISTIC_LOCK) {
            return "OPTIMISTIC";
        }
        return mechanism.name();
    }
}
