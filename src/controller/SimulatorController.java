package controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import model.Entity.FlashSaleEvent;
import model.Entity.FlashSaleItem;
import model.Entity.Order;
import model.Entity.OrderTransaction;
import model.Enum.LockMechanism;
import repository.CustomerRepository;
import repository.FlashSaleItemRepository;
import repository.FlashSaleRepository;
import repository.OrderRepository;
import repository.OrderTransactionRepository;
import repository.PaymentRepository;
import repository.ProductRepository;
import repository.UserRepository;
import view.SimulatorView;

public class SimulatorController {

    private final FlashSaleRepository flashSaleRepository;
    private final OrderRepository orderRepository;
    private final OrderTransactionRepository orderTransactionRepository;
    private final ProductRepository productRepository;
    private final FlashSaleItemRepository flashSaleItemRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;

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
        this.productRepository = new ProductRepository();
        this.flashSaleItemRepository = new FlashSaleItemRepository();
        this.paymentRepository = new PaymentRepository();
        this.userRepository = new UserRepository();
        this.customerRepository = new CustomerRepository();
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

    public Map<String, Integer> getDashboardStats() {
        Map<String, Integer> counts = new LinkedHashMap<String, Integer>();
        counts.put("Products", productRepository.findAll().size());
        counts.put("Flash events", flashSaleRepository.findAll().size());
        counts.put("Flash items", flashSaleItemRepository.findAll().size());
        counts.put("Orders", orderRepository.findAll().size());
        counts.put("Transactions", orderTransactionRepository.findAll().size());
        counts.put("Payments", paymentRepository.findAll().size());
        counts.put("Users", userRepository.findAll().size());
        counts.put("Customers", customerRepository.findAll().size());
        return counts;
    }

    public List<FlashSaleItem> getInventoryReport() {
        return flashSaleItemRepository.findAll();
    }

    public List<FlashSaleItem> getNegativeStockItems() {
        List<FlashSaleItem> result = new ArrayList<FlashSaleItem>();
        for (FlashSaleItem item : flashSaleItemRepository.findAll()) {
            if (item.getSoldQty() > item.getLimitedQty()) {
                result.add(item);
            }
        }
        return result;
    }

    public double getRetryRate() {
        List<OrderTransaction> transactions = orderTransactionRepository.findAll();
        if (transactions.isEmpty()) {
            return 0.0;
        }
        int retryTotal = 0;
        for (OrderTransaction transaction : transactions) {
            retryTotal += transaction.getRetryCount();
        }
        return retryTotal * 1.0 / transactions.size();
    }

    public double getNegativeStockRate() {
        List<FlashSaleItem> items = flashSaleItemRepository.findAll();
        if (items.isEmpty()) {
            return 0.0;
        }
        int negative = 0;
        for (FlashSaleItem item : items) {
            if (item.getSoldQty() > item.getLimitedQty()) {
                negative++;
            }
        }
        return negative * 100.0 / items.size();
    }

    public boolean exportSimulationResult(int threadCount, LockMechanism lock) {
        List<String> lines = new ArrayList<String>();
        lines.add("Flash Sale Simulation Result");
        lines.add("Generated at: " + LocalDateTime.now());
        lines.add("Thread count: " + threadCount);
        lines.add("Lock mechanism: " + lock);
        lines.add("TPS: " + measureTPS());
        lines.add("Transactions: " + orderTransactionRepository.findAll().size());

        try {
            java.nio.file.Files.write(java.nio.file.Paths.get("docs", "simulation_result.txt"), lines);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public List<OrderTransaction> getAllTransactions() {
        return orderTransactionRepository.findAll();
    }
}
