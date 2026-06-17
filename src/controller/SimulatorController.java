package controller;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import model.Entity.FlashSaleEvent;
import model.Entity.Order;
import model.Entity.OrderTransaction;
import model.Enum.LockMechanism;
import repository.FlashSaleRepository;
import repository.OrderRepository;
import repository.OrderTransactionRepository;
import view.SimulatorView;

public class SimulatorController {

    private final FlashSaleRepository flashSaleRepository;

    private final OrderRepository orderRepository;

    private final OrderTransactionRepository orderTransactionRepository;

    private final SimulatorView simulatorView;

    private List<OrderTransaction> latestTransactions;

    public SimulatorController() {
        this(
                new FlashSaleRepository(),
                new OrderRepository(),
                new OrderTransactionRepository(),
                new SimulatorView()
        );
    }

    public SimulatorController(FlashSaleRepository flashSaleRepository,
                               OrderRepository orderRepository,
                               SimulatorView simulatorView) {
        this(
                flashSaleRepository,
                orderRepository,
                new OrderTransactionRepository(),
                simulatorView
        );
    }

    public SimulatorController(FlashSaleRepository flashSaleRepository,
                               OrderRepository orderRepository,
                               OrderTransactionRepository orderTransactionRepository,
                               SimulatorView simulatorView) {
        this.flashSaleRepository = flashSaleRepository;
        this.orderRepository = orderRepository;
        this.orderTransactionRepository = orderTransactionRepository;
        this.simulatorView = simulatorView;
        this.latestTransactions = new ArrayList<OrderTransaction>();
    }

    public void startSimulation() {
        createThreads();
        runConcurrentOrders();
        generateSimulationReport();
    }

    public void createThreads() {
        List<FlashSaleEvent> events = flashSaleRepository.findAll();
        simulatorView.displayRaceConditionResult(
                "Loaded " + events.size() + " flash sale event(s) for simulation context."
        );
    }

    public void runConcurrentOrders() {
        latestTransactions = orderTransactionRepository.findAll();
        simulatorView.displaySimulationResult(latestTransactions);
    }

    public double measureTPS() {
        if (latestTransactions == null || latestTransactions.isEmpty()) {
            latestTransactions = orderTransactionRepository.findAll();
        }

        long totalExecutionTimeMs = 0L;
        int successCount = 0;

        for (OrderTransaction transaction : latestTransactions) {
            totalExecutionTimeMs += transaction.getExecutionTimeMs();

            if (transaction.isSuccess()) {
                successCount++;
            }
        }

        if (totalExecutionTimeMs <= 0L) {
            return 0.0;
        }

        return successCount * 1000.0 / totalExecutionTimeMs;
    }

    public void compareLockMechanisms() {
        if (latestTransactions == null || latestTransactions.isEmpty()) {
            latestTransactions = orderTransactionRepository.findAll();
        }

        Map<LockMechanism, Integer> successCountByMechanism =
                new EnumMap<LockMechanism, Integer>(LockMechanism.class);

        for (OrderTransaction transaction : latestTransactions) {
            if (!transaction.isSuccess()) {
                continue;
            }

            LockMechanism mechanism = transaction.getMechanism();
            Integer currentCount = successCountByMechanism.get(mechanism);
            successCountByMechanism.put(
                    mechanism,
                    currentCount == null ? 1 : currentCount + 1
            );
        }

        simulatorView.displayLockComparison(successCountByMechanism);
    }

    public void generateSimulationReport() {
        List<Order> orders = orderRepository.findAll();
        double tps = measureTPS();

        simulatorView.displayRaceConditionResult(
                "Total orders in repository: " + orders.size()
        );
        simulatorView.displayTPSReport(tps);
        compareLockMechanisms();
    }
}
