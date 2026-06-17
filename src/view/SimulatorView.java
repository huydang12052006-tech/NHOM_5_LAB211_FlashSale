package view;

import java.util.List;
import java.util.Map;

import model.Entity.OrderTransaction;
import model.Enum.LockMechanism;

public class SimulatorView {

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
