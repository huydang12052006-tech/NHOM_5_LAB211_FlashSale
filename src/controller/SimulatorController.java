package controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
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


    private static final int REPEAT_RUNS = 3;
    private static final int QUANTITY_PER_ORDER = 1;
    private static final int OPTIMISTIC_RETRY_LIMIT = 20;
    private static final int FILE_LOCK_DELAY_MS = 3;
    private static final int SIMULATED_BUSINESS_LOGIC_ITERATIONS = 500_000;
    private static volatile double simulatedWorkloadSink = 0.0;

    private final FlashSaleRepository flashSaleRepository;
    private final OrderRepository orderRepository;
    private final OrderTransactionRepository orderTransactionRepository;
    private final ProductRepository productRepository;
    private final FlashSaleItemRepository flashSaleItemRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final Object fileLockGate;

    private final SimulatorView simulatorView;
    private List<OrderTransaction> latestTransactions;
    private List<SimulationSummary> latestSummaries;

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
        this.fileLockGate = new Object();
        this.simulatorView = simulatorView;
        this.latestTransactions = new ArrayList<OrderTransaction>();
        this.latestSummaries = new ArrayList<SimulationSummary>();
    }

    public void startSimulation() {
        startSimulation(2);
    }

    public List<SimulationSummary> startSimulation(int threadCount) {
        return startSimulation(threadCount, null);
    }

    public List<SimulationSummary> startSimulation(int threadCount, LockMechanism selectedMechanism) {
        FlashSaleItem targetItem = findSimulationTarget();

        if (targetItem == null) {
            simulatorView.displayRaceConditionResult("No flash sale item found for simulation.");
            latestTransactions = new ArrayList<OrderTransaction>();
            latestSummaries = new ArrayList<SimulationSummary>();
            return latestSummaries;
        }

        orderTransactionRepository.clearFile();

        int stockLimit = calculateInitialStock(threadCount);
        List<BenchmarkScenario> scenarios = createBenchmarkScenarios(threadCount, stockLimit);
        AtomicInteger transactionSequence = new AtomicInteger(findMaxTransactionNumber() + 1);
        List<SimulationSummary> summaries = new ArrayList<SimulationSummary>();
        List<OrderTransaction> newTransactions = new ArrayList<OrderTransaction>();

        simulatorView.showBenchmarkDashboard(
                threadCount,
                scenarios.size(),
                stockLimit,
                threadCount,
                REPEAT_RUNS
        );

        List<LockMechanism> mechanismsToRun;
        if (selectedMechanism == null) {
            mechanismsToRun = Arrays.asList(LockMechanism.values());
        } else {
            mechanismsToRun = Arrays.asList(selectedMechanism);
        }

        for (BenchmarkScenario scenario : scenarios) {
            for (LockMechanism mechanism : mechanismsToRun) {
                simulatorView.showBenchmarkProgressStart(mechanism);

                List<SimulationSummary> repeatedSummaries = new ArrayList<SimulationSummary>();
                List<OrderTransaction> mechanismTransactions = new ArrayList<OrderTransaction>();

                for (int repeat = 1; repeat <= REPEAT_RUNS; repeat++) {
                    InMemoryInventory inventory = new InMemoryInventory(
                            scenario.getProductCount(),
                            scenario.getStockPerProduct()
                    );
                    SimulationRun run = runConcurrentOrders(
                            inventory,
                            targetItem.getId(),
                            threadCount,
                            scenario,
                            mechanism,
                            repeat,
                            transactionSequence
                    );
                    repeatedSummaries.add(run.summary);
                    mechanismTransactions.addAll(run.transactions);
                }

                summaries.add(aggregateSummaries(
                        scenario,
                        mechanism,
                        threadCount,
                        repeatedSummaries,
                        mechanismTransactions
                ));
                newTransactions.addAll(mechanismTransactions);
                simulatorView.showBenchmarkProgressDone();
            }
        }

        persistTransactions(newTransactions);
        persistBenchmarkSummary(summaries);
        latestTransactions = newTransactions;
        latestSummaries = summaries;
        simulatorView.displayLockComparison(summaries);
        simulatorView.displayResearchQuestionEvaluation(summaries);
        return summaries;
    }

    public void createThreads() {
        List<FlashSaleEvent> events = flashSaleRepository.findAll();
        simulatorView.displayRaceConditionResult(
                "Loaded " + events.size() + " flash sale event(s) for simulation context."
        );
    }

    public void runConcurrentOrders() {
        startSimulation(2);
    }

    public double measureTPS() {
        if (latestSummaries != null && !latestSummaries.isEmpty()) {
            int validSuccessCount = 0;
            long elapsedNanos = 0L;

            for (SimulationSummary summary : latestSummaries) {
                validSuccessCount += summary.getValidSuccessCount();
                elapsedNanos += summary.getElapsedNanos();
            }

            return calculateTps(validSuccessCount, elapsedNanos);
        }

        return 0.0;
    }

    public void compareLockMechanisms() {
        if (latestSummaries != null && !latestSummaries.isEmpty()) {
            simulatorView.displayLockComparison(latestSummaries);
            return;
        }

        Map<LockMechanism, Integer> successCountByMechanism =
                new EnumMap<LockMechanism, Integer>(LockMechanism.class);

        for (OrderTransaction transaction : getAllTransactions()) {
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

        simulatorView.displayRaceConditionResult(
                "Total orders in repository: " + orders.size()
        );
        simulatorView.displayTPSReport(measureTPS());
        compareLockMechanisms();
    }

    public Map<String, Integer> getDashboardStats() {
        Map<String, Integer> counts = new LinkedHashMap<String, Integer>();
        counts.put("Products", productRepository.findAll().size());
        counts.put("Flash events", flashSaleRepository.findAll().size());
        counts.put("Flash items", flashSaleItemRepository.findAll().size());
        counts.put("Orders", orderRepository.findAll().size());
        counts.put("Transactions", orderTransactionRepository.findAll().size());
        counts.put("PaymentTransactions", paymentRepository.findAll().size());
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
        List<OrderTransaction> transactions = getLatestOrStoredTransactions();
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
        if (latestSummaries != null && !latestSummaries.isEmpty()) {
            int oversell = 0;
            for (SimulationSummary summary : latestSummaries) {
                if (summary.getNegativeCount() > 0) {
                    oversell++;
                }
            }
            return oversell * 100.0 / latestSummaries.size();
        }

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
        lines.add("Benchmark inventory mode: IN_MEMORY");
        lines.add("CSV mode: persistence only");
        lines.add("Thread count: " + threadCount);
        lines.add("Configured lock mechanism: " + lock);
        lines.add("Measured TPS: " + String.format(Locale.US, "%.2f", measureTPS()));
        lines.add("New transactions in latest run: " + latestTransactions.size());

        for (SimulationSummary summary : latestSummaries) {
            lines.add(summary.toExportLine());
        }

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

    public List<OrderTransaction> getLatestTransactions() {
        return new ArrayList<OrderTransaction>(latestTransactions);
    }

    public long getLatestElapsedMs() {
        if (latestSummaries == null || latestSummaries.isEmpty()) {
            return 0L;
        }

        long elapsedNanos = 0L;
        for (SimulationSummary summary : latestSummaries) {
            elapsedNanos += summary.getElapsedNanos();
        }

        return TimeUnit.NANOSECONDS.toMillis(elapsedNanos);
    }

    public int getLatestValidSuccessCount() {
        if (latestSummaries == null || latestSummaries.isEmpty()) {
            return countSuccessfulTransactions(getLatestOrStoredTransactions());
        }

        int validSuccessCount = 0;
        for (SimulationSummary summary : latestSummaries) {
            validSuccessCount += summary.getValidSuccessCount();
        }
        return validSuccessCount;
    }

    public int getLatestRawSuccessCount() {
        return countSuccessfulTransactions(getLatestOrStoredTransactions());
    }

    private SimulationRun runConcurrentOrders(InMemoryInventory inventory,
                                              String flashItemId,
                                              int threadCount,
                                              BenchmarkScenario scenario,
                                              LockMechanism mechanism,
                                              int repeatNumber,
                                              AtomicInteger transactionSequence) {
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        Queue<OrderTransaction> transactions = new ConcurrentLinkedQueue<OrderTransaction>();
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        AtomicInteger retryCount = new AtomicInteger(0);
        AtomicInteger logicalSuccessCounter = new AtomicInteger(0);

        for (int i = 1; i <= threadCount; i++) {
            final int workerNumber = i;
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    readyLatch.countDown();
                    try {
                        startLatch.await();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        doneLatch.countDown();
                        return;
                    }

                    OrderTransaction transaction = executeSimulatedOrder(
                            inventory,
                            flashItemId,
                            mechanism,
                            scenario,
                            workerNumber,
                            repeatNumber,
                            logicalSuccessCounter,
                            transactionSequence
                    );

                    if (transaction.isSuccess()) {
                        successCount.incrementAndGet();
                    } else {
                        failureCount.incrementAndGet();
                    }
                    retryCount.addAndGet(transaction.getRetryCount());
                    transactions.add(transaction);
                    doneLatch.countDown();
                }
            });
        }

        try {
            readyLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        long startNanos = System.nanoTime();
        startLatch.countDown();

        try {
            doneLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        long elapsedNanos = System.nanoTime() - startNanos;
        executorService.shutdown();

        int finalSoldQty = inventory.getSoldQty(mechanism);
        long adjustedElapsedNanos = calculateAdjustedElapsedNanos(
                elapsedNanos,
                transactions,
                threadCount
        );
        SimulationSummary summary = new SimulationSummary(
                scenario.getName(),
                mechanism,
                threadCount,
                scenario.getProductCount(),
                scenario.getTotalStock(),
                successCount.get(),
                failureCount.get(),
                retryCount.get(),
                countNegativeTransactions(transactions),
                elapsedNanos,
                averageExecutionTimeMs(transactions),
                maxExecutionTimeMs(transactions),
                finalSoldQty
        );

        return new SimulationRun(summary, new ArrayList<OrderTransaction>(transactions));
    }

    private OrderTransaction executeSimulatedOrder(InMemoryInventory inventory,
                                                   String flashItemId,
                                                   LockMechanism mechanism,
                                                   BenchmarkScenario scenario,
                                                   int workerNumber,
                                                   int repeatNumber,
                                                   AtomicInteger logicalSuccessCounter,
                                                   AtomicInteger transactionSequence) {
        long started = System.nanoTime();
        int productIndex = selectProductIndex(workerNumber, repeatNumber, scenario.getProductCount());

        return executeSimulatedOrderInternal(
                started,
                inventory,
                flashItemId,
                mechanism,
                scenario,
                productIndex,
                workerNumber,
                repeatNumber,
                logicalSuccessCounter,
                transactionSequence
        );
    }

    private OrderTransaction executeSimulatedOrderInternal(long started,
                                                           InMemoryInventory inventory,
                                                           String flashItemId,
                                                           LockMechanism mechanism,
                                                           BenchmarkScenario scenario,
                                                           int productIndex,
                                                           int workerNumber,
                                                           int repeatNumber,
                                                           AtomicInteger logicalSuccessCounter,
                                                           AtomicInteger transactionSequence) {
        SellResult result = sell(inventory, mechanism, productIndex);

        if (result.success) {
            int successIndex = logicalSuccessCounter.incrementAndGet();
            if (mechanism != LockMechanism.NO_LOCK) {
                result.stockBefore = scenario.getTotalStock() - successIndex + QUANTITY_PER_ORDER;
                result.stockAfter = scenario.getTotalStock() - successIndex;
            }
        }

        long executionTimeMs = Math.max(1L, TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started));
        LocalDateTime now = LocalDateTime.now();
        String transactionId = String.format("TR%06d", transactionSequence.getAndIncrement());
        String orderId = "SIM-" + mechanism.name() + "-R" + repeatNumber + "-" + workerNumber;

        return new OrderTransaction(
                transactionId,
                now,
                now,
                orderId,
                Thread.currentThread().getName(),
                mechanism,
                result.success,
                result.retryCount,
                executionTimeMs,
                result.negativeWriteTimeMs,
                result.stockBefore,
                result.stockAfter,
                result.versionBefore,
                result.versionAfter,
                result.message
        );
    }

    private SellResult sell(InMemoryInventory inventory,
                            LockMechanism mechanism,
                            int productIndex) {
        if (mechanism == LockMechanism.SYNCHRONIZED) {
            return inventory.sellWithSynchronized(productIndex, QUANTITY_PER_ORDER);
        }
        if (mechanism == LockMechanism.FILE_LOCK) {
            synchronized (fileLockGate) {
                simulateFileLockDelay();
                return inventory.sellWithFileLock(productIndex, QUANTITY_PER_ORDER);
            }
        }
        if (mechanism == LockMechanism.OPTIMISTIC_LOCK) {
            return inventory.sellWithOptimisticLock(productIndex, QUANTITY_PER_ORDER);
        }
        return inventory.sellWithNoLock(productIndex, QUANTITY_PER_ORDER);
    }

    private void persistTransactions(List<OrderTransaction> transactions) {
        orderTransactionRepository.ensureBenchmarkHeader();
        for (OrderTransaction transaction : transactions) {
            orderTransactionRepository.save(transaction);
        }
    }

    private void persistBenchmarkSummary(List<SimulationSummary> summaries) {
        List<String> lines = new ArrayList<String>();
        lines.add("scenario,mechanism,threads,products,totalStock,tps,avgLatencyMs,maxLatencyMs,"
                + "retry,oversell,success,failed,finalSold,goal");

        for (SimulationSummary summary : summaries) {
            lines.add(summary.toSummaryCsvLine());
        }

        try {
            java.nio.file.Files.write(java.nio.file.Paths.get("data", "benchmark_summary.csv"), lines);
        } catch (IOException e) {
            System.out.println("[ERROR] Unable to write benchmark summary: " + e.getMessage());
        }
    }

    private List<OrderTransaction> getLatestOrStoredTransactions() {
        if (latestTransactions != null && !latestTransactions.isEmpty()) {
            return latestTransactions;
        }
        return orderTransactionRepository.findAll();
    }

    private List<BenchmarkScenario> createBenchmarkScenarios(int threadCount, int stockPerProduct) {
        List<BenchmarkScenario> scenarios = new ArrayList<BenchmarkScenario>();
        scenarios.add(new BenchmarkScenario("HIGH_CONTENTION", 1, stockPerProduct));
        return scenarios;
    }

    private int selectProductIndex(int workerNumber, int repeatNumber, int productCount) {
        return Math.floorMod(workerNumber + repeatNumber, productCount);
    }

    private void simulateFileLockDelay() {
        if (FILE_LOCK_DELAY_MS <= 0) {
            return;
        }
        try {
            Thread.sleep(FILE_LOCK_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void simulateBusinessLogic() {
        double value = 0.0;
        for (int i = 0; i < SIMULATED_BUSINESS_LOGIC_ITERATIONS; i++) {
            value += Math.sqrt(i + 1);
        }
        simulatedWorkloadSink = value;
    }

    private FlashSaleItem findSimulationTarget() {
        List<FlashSaleItem> items = flashSaleItemRepository.findAll();
        if (items.isEmpty()) {
            return null;
        }

        FlashSaleItem selected = items.get(0);
        for (FlashSaleItem item : items) {
            if (item.getLimitedQty() > selected.getLimitedQty()) {
                selected = item;
            }
        }
        return selected;
    }



    private int calculateInitialStock(int threadCount) {
        return Math.max(1, threadCount / 5);
    }

    private int findMaxTransactionNumber() {
        int max = 0;
        for (OrderTransaction transaction : orderTransactionRepository.findAll()) {
            String id = transaction.getId();
            if (id != null && id.matches("(TX|TR)\\d+")) {
                max = Math.max(max, Integer.parseInt(id.substring(2)));
            }
        }
        return max;
    }

    private SimulationSummary aggregateSummaries(BenchmarkScenario scenario,
                                                 LockMechanism mechanism,
                                                 int threadCount,
                                                 List<SimulationSummary> summaries,
                                                 List<OrderTransaction> transactions) {
        int successCount = 0;
        int failureCount = 0;
        int retryCount = 0;
        int negativeCount = 0;
        long elapsedNanos = 0L;
        int finalSoldQty = 0;

        for (SimulationSummary summary : summaries) {
            successCount += summary.getSuccessCount();
            failureCount += summary.getFailureCount();
            retryCount += summary.getRetryCount();
            negativeCount += summary.getNegativeCount();
            elapsedNanos += summary.getElapsedNanos();
            finalSoldQty = Math.max(finalSoldQty, summary.getFinalSoldQty());
        }

        return new SimulationSummary(
                scenario.getName(),
                mechanism,
                threadCount,
                scenario.getProductCount(),
                scenario.getTotalStock(),
                successCount,
                failureCount,
                retryCount,
                negativeCount,
                elapsedNanos,
                averageExecutionTimeMs(transactions),
                maxExecutionTimeMs(transactions),
                finalSoldQty
        );
    }

    private int countNegativeTransactions(Iterable<OrderTransaction> transactions) {
        int count = 0;
        for (OrderTransaction transaction : transactions) {
            if (transaction.getStockAfter() < 0) {
                count++;
            }
        }
        return count;
    }

    private int countSuccessfulTransactions(Iterable<OrderTransaction> transactions) {
        int count = 0;
        for (OrderTransaction transaction : transactions) {
            if (transaction.isSuccess()) {
                count++;
            }
        }
        return count;
    }

    private double averageExecutionTimeMs(Iterable<OrderTransaction> transactions) {
        long total = 0L;
        int count = 0;
        for (OrderTransaction transaction : transactions) {
            total += transaction.getExecutionTimeMs();
            count++;
        }
        return count == 0 ? 0.0 : total * 1.0 / count;
    }

    long calculateAdjustedElapsedNanos(long elapsedNanos,
                                       Iterable<OrderTransaction> transactions,
                                       int threadCount) {
        long negativeExecutionMs = 0L;
        int count = 0;
        for (OrderTransaction transaction : transactions) {
            negativeExecutionMs += transaction.getNegativeWriteTimeMs();
            count++;
        }

        long avgNegativePerThreadMs = threadCount > 0 ? (negativeExecutionMs / threadCount) : 0L;
        return Math.max(1L, elapsedNanos - TimeUnit.MILLISECONDS.toNanos(avgNegativePerThreadMs));
    }

    private long maxExecutionTimeMs(Iterable<OrderTransaction> transactions) {
        long max = 0L;
        for (OrderTransaction transaction : transactions) {
            max = Math.max(max, transaction.getExecutionTimeMs());
        }
        return max;
    }

    private static double calculateTps(int successCount, long elapsedNanos) {
        if (successCount <= 0 || elapsedNanos <= 0L) {
            return 0.0;
        }
        return successCount * 1_000_000_000.0 / elapsedNanos;
    }

    private static class BenchmarkScenario {
        private final String name;
        private final int productCount;
        private final int stockPerProduct;

        private BenchmarkScenario(String name, int productCount, int stockPerProduct) {
            this.name = name;
            this.productCount = productCount;
            this.stockPerProduct = stockPerProduct;
        }

        private String getName() {
            return name;
        }

        private int getProductCount() {
            return productCount;
        }

        private int getStockPerProduct() {
            return stockPerProduct;
        }

        private int getTotalStock() {
            return productCount * stockPerProduct;
        }
    }

    private static class InMemoryInventory {
        private final int productCount;
        private final int stockPerProduct;
        private final Object[] monitors;
        private final AtomicReference<InventoryState>[] optimisticStates;
        private final int[] soldQtyByProduct;
        private final int[] versionByProduct;

        @SuppressWarnings("unchecked")
        private InMemoryInventory(int productCount, int stockPerProduct) {
            this.productCount = productCount;
            this.stockPerProduct = stockPerProduct;
            this.monitors = new Object[productCount];
            this.optimisticStates = new AtomicReference[productCount];
            this.soldQtyByProduct = new int[productCount];
            this.versionByProduct = new int[productCount];

            for (int i = 0; i < productCount; i++) {
                monitors[i] = new Object();
                optimisticStates[i] = new AtomicReference<InventoryState>(new InventoryState(0, 1));
                versionByProduct[i] = 1;
            }
        }

        private SellResult sellWithNoLock(int productIndex, int quantity) {
            int observedSold = soldQtyByProduct[productIndex];
            int observedVersion = versionByProduct[productIndex];

            if (observedSold + quantity > stockPerProduct) {
            return SellResult.failed(
                stockPerProduct - observedSold,
                stockPerProduct - observedSold,
                observedVersion,
                observedVersion,
                "Not enough stock"
            );
            }

            simulateBusinessLogic();

            int actualSoldBefore = soldQtyByProduct[productIndex];
            int actualVersionBefore = versionByProduct[productIndex];
            long writeStartNanos = System.nanoTime();
            soldQtyByProduct[productIndex] = actualSoldBefore + quantity;
            versionByProduct[productIndex] = actualVersionBefore + 1;
            long writeTimeMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - writeStartNanos);
            int stockAfter = stockPerProduct - soldQtyByProduct[productIndex];
            long negativeWriteTimeMs = stockAfter < 0 ? Math.max(1L, writeTimeMs) : 0L;

            return SellResult.success(
                stockPerProduct - actualSoldBefore,
                stockAfter,
                actualVersionBefore,
                versionByProduct[productIndex],
                0,
                negativeWriteTimeMs
            );
        }

        private SellResult sellWithSynchronized(int productIndex, int quantity) {
            synchronized (monitors[productIndex]) {
                return sellWithPlainState(productIndex, quantity);
            }
        }

        private SellResult sellWithFileLock(int productIndex, int quantity) {
            return sellWithPlainState(productIndex, quantity);
        }

        private SellResult sellWithOptimisticLock(int productIndex, int quantity) {
            int retry = 0;

            while (retry <= OPTIMISTIC_RETRY_LIMIT) {
                AtomicReference<InventoryState> stateRef = optimisticStates[productIndex];
                InventoryState current = stateRef.get();

                if (current.soldQty + quantity > stockPerProduct) {
                    return SellResult.failed(
                            stockPerProduct - current.soldQty,
                            stockPerProduct - current.soldQty,
                            current.version,
                            current.version,
                            "Not enough stock",
                            retry
                    );
                }

            // simulate business logic between read and write to model contention window
            simulateBusinessLogic();

            InventoryState next = new InventoryState(
                current.soldQty + quantity,
                current.version + 1
            );

            if (stateRef.compareAndSet(current, next)) {
                return SellResult.success(
                    stockPerProduct - current.soldQty,
                    stockPerProduct - next.soldQty,
                    current.version,
                    next.version,
                    retry
                );
            }

            retry++;
            Thread.yield();
            }

            InventoryState latest = optimisticStates[productIndex].get();
            return SellResult.failed(
                    stockPerProduct - latest.soldQty,
                    stockPerProduct - latest.soldQty,
                    latest.version,
                    latest.version,
                    "Optimistic lock failed after " + OPTIMISTIC_RETRY_LIMIT + " retries",
                    retry
            );
        }

        private SellResult sellWithPlainState(int productIndex, int quantity) {
            int soldBefore = soldQtyByProduct[productIndex];
            int versionBefore = versionByProduct[productIndex];

            if (soldBefore + quantity > stockPerProduct) {
                return SellResult.failed(
                        stockPerProduct - soldBefore,
                        stockPerProduct - soldBefore,
                        versionBefore,
                        versionBefore,
                        "Not enough stock"
                );
            }

            // simulate business logic inside critical section / plain update
            simulateBusinessLogic();

            soldQtyByProduct[productIndex] = soldBefore + quantity;
            versionByProduct[productIndex] = versionBefore + 1;

            return SellResult.success(
                    stockPerProduct - soldBefore,
                    stockPerProduct - soldQtyByProduct[productIndex],
                    versionBefore,
                    versionByProduct[productIndex],
                    0
            );
        }

        private int getSoldQty(LockMechanism mechanism) {
            if (mechanism == LockMechanism.OPTIMISTIC_LOCK) {
                int total = 0;
                for (AtomicReference<InventoryState> state : optimisticStates) {
                    total += state.get().soldQty;
                }
                return total;
            }

            int total = 0;
            for (int soldQty : soldQtyByProduct) {
                total += soldQty;
            }
            return total;
        }
    }

    private static class InventoryState {
        private final int soldQty;
        private final int version;

        private InventoryState(int soldQty, int version) {
            this.soldQty = soldQty;
            this.version = version;
        }
    }

    private static class SellResult {
        private final boolean success;
        private final int retryCount;
        private final long negativeWriteTimeMs;
        private int stockBefore;
        private int stockAfter;
        private final int versionBefore;
        private final int versionAfter;
        private final String message;

        private SellResult(boolean success,
                           int stockBefore,
                           int stockAfter,
                           int versionBefore,
                           int versionAfter,
                           int retryCount,
                           String message,
                           long negativeWriteTimeMs) {
            this.success = success;
            this.stockBefore = stockBefore;
            this.stockAfter = stockAfter;
            this.versionBefore = versionBefore;
            this.versionAfter = versionAfter;
            this.retryCount = retryCount;
            this.message = message;
            this.negativeWriteTimeMs = negativeWriteTimeMs;
        }

        private static SellResult success(int stockBefore,
                                          int stockAfter,
                                          int versionBefore,
                                          int versionAfter,
                                          int retryCount) {
            return success(stockBefore, stockAfter, versionBefore, versionAfter, retryCount, 0L);
        }

        private static SellResult success(int stockBefore,
                                          int stockAfter,
                                          int versionBefore,
                                          int versionAfter,
                                          int retryCount,
                                          long negativeWriteTimeMs) {
            return new SellResult(
                    true,
                    stockBefore,
                    stockAfter,
                    versionBefore,
                    versionAfter,
                    retryCount,
                    "Order processed successfully",
                    negativeWriteTimeMs
            );
        }

        private static SellResult failed(int stockBefore,
                                         int stockAfter,
                                         int versionBefore,
                                         int versionAfter,
                                         String message) {
            return failed(stockBefore, stockAfter, versionBefore, versionAfter, message, 0);
        }

        private static SellResult failed(int stockBefore,
                                         int stockAfter,
                                         int versionBefore,
                                         int versionAfter,
                                         String message,
                                         int retryCount) {
            return failed(stockBefore, stockAfter, versionBefore, versionAfter, message, retryCount, 0L);
        }

        private static SellResult failed(int stockBefore,
                                         int stockAfter,
                                         int versionBefore,
                                         int versionAfter,
                                         String message,
                                         int retryCount,
                                         long negativeWriteTimeMs) {
            return new SellResult(
                    false,
                    stockBefore,
                    stockAfter,
                    versionBefore,
                    versionAfter,
                    retryCount,
                    message,
                    negativeWriteTimeMs
            );
        }
    }

    private static class SimulationRun {
        private final SimulationSummary summary;
        private final List<OrderTransaction> transactions;

        private SimulationRun(SimulationSummary summary, List<OrderTransaction> transactions) {
            this.summary = summary;
            this.transactions = transactions;
        }
    }

    public static class SimulationSummary {
        private final String scenarioName;
        private final LockMechanism mechanism;
        private final int threadCount;
        private final int productCount;
        private final int stockLimit;
        private final int successCount;
        private final int failureCount;
        private final int retryCount;
        private final int negativeCount;
        private final long elapsedNanos;
        private final double averageExecutionTimeMs;
        private final long maxExecutionTimeMs;
        private final int finalSoldQty;

        public SimulationSummary(String scenarioName,
                                 LockMechanism mechanism,
                                 int threadCount,
                                 int productCount,
                                 int stockLimit,
                                 int successCount,
                                 int failureCount,
                                 int retryCount,
                                 int negativeCount,
                                 long elapsedNanos,
                                 double averageExecutionTimeMs,
                                 long maxExecutionTimeMs,
                                 int finalSoldQty) {
            this.scenarioName = scenarioName;
            this.mechanism = mechanism;
            this.threadCount = threadCount;
            this.productCount = productCount;
            this.stockLimit = stockLimit;
            this.successCount = successCount;
            this.failureCount = failureCount;
            this.retryCount = retryCount;
            this.negativeCount = negativeCount;
            this.elapsedNanos = elapsedNanos;
            this.averageExecutionTimeMs = averageExecutionTimeMs;
            this.maxExecutionTimeMs = maxExecutionTimeMs;
            this.finalSoldQty = finalSoldQty;
        }

        public LockMechanism getMechanism() {
            return mechanism;
        }

        public String getScenarioName() {
            return scenarioName;
        }

        public int getThreadCount() {
            return threadCount;
        }

        public int getStockLimit() {
            return stockLimit;
        }

        public int getProductCount() {
            return productCount;
        }

        public int getSuccessCount() {
            return successCount;
        }

        public int getFailureCount() {
            return failureCount;
        }

        public int getRetryCount() {
            return retryCount;
        }

        public int getNegativeCount() {
            return negativeCount;
        }

        public long getElapsedNanos() {
            return elapsedNanos;
        }

        public long getElapsedMs() {
            return Math.max(1L, TimeUnit.NANOSECONDS.toMillis(elapsedNanos));
        }

        public int getFinalSoldQty() {
            return finalSoldQty;
        }

        public double getAverageExecutionTimeMs() {
            return averageExecutionTimeMs;
        }

        public long getMaxExecutionTimeMs() {
            return maxExecutionTimeMs;
        }

        public double getTps() {
            return calculateTps(successCount, elapsedNanos);
        }

        public int getValidSuccessCount() {
            int repeatCount = Math.max(1, (successCount + failureCount) / Math.max(1, threadCount));
            return Math.min(successCount, stockLimit * repeatCount);
        }

        public double getValidTps() {
            return calculateTps(getValidSuccessCount(), elapsedNanos);
        }

        public boolean isOversold() {
            return negativeCount > 0 || finalSoldQty > stockLimit;
        }

        public String getGoal() {
            return negativeCount == 0 ? "PASS" : "FAIL";
        }

        public String toExportLine() {
            return mechanism
                    + " | scenario=" + scenarioName
                    + " | threads=" + threadCount
                    + " | products=" + productCount
                    + " | stock=" + stockLimit
                    + " | success=" + successCount
                    + " | validSuccess=" + getValidSuccessCount()
                    + " | fail=" + failureCount
                    + " | elapsedMs=" + getElapsedMs()
                    + " | TPS=" + String.format(Locale.US, "%.2f", getValidTps())
                    + " | negative=" + negativeCount
                    + " | avgMs=" + String.format(Locale.US, "%.2f", averageExecutionTimeMs)
                    + " | maxMs=" + maxExecutionTimeMs
                    + " | finalSold=" + finalSoldQty
                    + " | oversold=" + isOversold();
        }

        public String toSummaryCsvLine() {
            return String.join(",",
                    scenarioName,
                    mechanism.name(),
                    String.valueOf(threadCount),
                    String.valueOf(productCount),
                    String.valueOf(stockLimit),
                    String.format(Locale.US, "%.2f", getValidTps()),
                    String.format(Locale.US, "%.2f", averageExecutionTimeMs),
                    String.valueOf(maxExecutionTimeMs),
                    String.valueOf(retryCount),
                    String.valueOf(negativeCount),
                    String.valueOf(successCount),
                    String.valueOf(failureCount),
                    String.valueOf(finalSoldQty),
                    getGoal()
            );
        }
    }
}
