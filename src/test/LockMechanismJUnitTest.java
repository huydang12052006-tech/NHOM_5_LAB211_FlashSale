import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import exception.FlashSaleException;
import exception.OutOfStockException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import model.Entity.FlashSaleItem;
import model.Enum.SaleStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import repository.FlashSaleItemRepository;

public class LockMechanismJUnitTest {

    private static final Path TEST_DIR =
            Paths.get("data", "test_lock_mechanism_junit");
    private static final LocalDateTime CREATED_AT =
            LocalDateTime.of(2026, 1, 2, 3, 4, 5);
    private static final LocalDateTime UPDATED_AT =
            LocalDateTime.of(2026, 2, 3, 4, 5, 6);
    private static final String HEADER =
            "id,createdAt,updatedAt,eventId,productId,flashPrice,"
                    + "limitedQty,soldQty,discountPercent,version,status";

    @BeforeEach
    void createTestDirectory() throws IOException {
        Files.createDirectories(TEST_DIR);
    }

    @AfterEach
    void deleteTestFiles() throws IOException {
        if (!Files.exists(TEST_DIR)) {
            return;
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(TEST_DIR)) {
            for (Path path : stream) {
                Files.deleteIfExists(path);
            }
        }

        Files.deleteIfExists(TEST_DIR);
    }

    @Test
    void noLockSellsWhenStockIsAvailable() throws Exception {
        FlashSaleItemRepository repository =
                prepareRepository("no_lock.csv", 2, 10, 7);

        assertTrue(repository.sellWithNoLock("FI_LOCK", 3));

        assertFlashItemState(repository, 5, 7);
    }

    @Test
    void synchronizedLockSellsWhenStockIsAvailable() throws Exception {
        FlashSaleItemRepository repository =
                prepareRepository("synchronized.csv", 2, 10, 7);

        assertTrue(repository.sellWithSynchronized("FI_LOCK", 3));

        assertFlashItemState(repository, 5, 7);
    }

    @Test
    void fileLockSellsWhenStockIsAvailable() throws Exception {
        FlashSaleItemRepository repository =
                prepareRepository("file_lock.csv", 2, 10, 7);

        assertTrue(repository.sellWithFileLock("FI_LOCK", 3));

        assertFlashItemState(repository, 5, 7);
    }

    @Test
    void optimisticLockSellsAndIncrementsVersionWhenStockIsAvailable()
            throws Exception {

        FlashSaleItemRepository repository =
                prepareRepository("optimistic_lock.csv", 2, 10, 7);

        assertTrue(repository.sellWithOptimisticLock("FI_LOCK", 3));

        assertFlashItemState(repository, 5, 8);
    }

    @Test
    void noLockAllowsTwoThreadsToSellSameLastStock()
            throws Exception {

        FlashSaleItemRepository repository =
                prepareRepository("no_lock_concurrent.csv", 0, 6, 1);

        ConcurrentResult result = runTwoConcurrentSales(
                new SaleAction() {
                    @Override
                    public boolean sell() throws Exception {
                        return repository.sellWithNoLock("FI_LOCK", 6);
                    }
                }
        );

        assertEquals(2, result.successCount.get());
        assertEquals(0, result.expectedFailures.size());
        assertEquals(0, result.unexpectedFailures.size());
        assertFlashItemState(repository, 6, 1);
    }

    @Test
    void synchronizedLockDoesNotOversellWhenTwoThreadsBuySameLastStock()
            throws Exception {

        FlashSaleItemRepository repository =
                prepareRepository("synchronized_concurrent.csv", 0, 6, 1);

        ConcurrentResult result = runTwoConcurrentSales(
                new SaleAction() {
                    @Override
                    public boolean sell() throws Exception {
                        return repository.sellWithSynchronized("FI_LOCK", 6);
                    }
                }
        );

        assertEquals(1, result.successCount.get());
        assertEquals(1, result.expectedFailures.size());
        assertEquals(0, result.unexpectedFailures.size());
        assertFlashItemState(repository, 6, 1);
    }

    @Test
    void optimisticLockDoesNotOversellWhenTwoThreadsBuySameLastStock()
            throws Exception {

        FlashSaleItemRepository repository =
                prepareRepository("optimistic_concurrent.csv", 0, 6, 1);

        ConcurrentResult result = runTwoConcurrentSales(
                new SaleAction() {
                    @Override
                    public boolean sell() throws Exception {
                        return repository.sellWithOptimisticLock("FI_LOCK", 6);
                    }
                }
        );

        assertEquals(1, result.successCount.get());
        assertEquals(1, result.expectedFailures.size());
        assertEquals(0, result.unexpectedFailures.size());
        assertFlashItemState(repository, 6, 2);
    }

    @Test
    void fileLockRejectsSecondSaleWhenStockIsAlreadyConsumed()
            throws Exception {

        FlashSaleItemRepository repository =
                prepareRepository("file_lock_out_of_stock.csv", 0, 6, 1);

        assertTrue(repository.sellWithFileLock("FI_LOCK", 6));
        assertThrows(
                OutOfStockException.class,
                () -> repository.sellWithFileLock("FI_LOCK", 1)
        );

        assertFlashItemState(repository, 6, 1);
    }

    private static ConcurrentResult runTwoConcurrentSales(
            SaleAction action
    ) throws InterruptedException {

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(2);
        ConcurrentResult result = new ConcurrentResult();

        Runnable task = new Runnable() {
            @Override
            public void run() {
                ready.countDown();
                try {
                    start.await();
                    if (action.sell()) {
                        result.successCount.incrementAndGet();
                    }
                } catch (OutOfStockException e) {
                    result.expectedFailures.add(e);
                } catch (FlashSaleException e) {
                    result.expectedFailures.add(e);
                } catch (Throwable e) {
                    result.unexpectedFailures.add(e);
                } finally {
                    done.countDown();
                }
            }
        };

        new Thread(task, "lock-test-1").start();
        new Thread(task, "lock-test-2").start();

        ready.await();
        start.countDown();
        done.await();

        return result;
    }

    private static FlashSaleItemRepository prepareRepository(
            String fileName,
            int soldQty,
            int limitedQty,
            int version
    ) throws IOException {

        String filePath = TEST_DIR.resolve(fileName).toString();
        resetFile(filePath);

        FlashSaleItemRepository repository =
                new FlashSaleItemRepository(filePath);

        repository.save(new FlashSaleItem(
                "FI_LOCK",
                CREATED_AT,
                UPDATED_AT,
                "E_LOCK",
                "P_LOCK",
                990.0,
                limitedQty,
                soldQty,
                10.0,
                version,
                SaleStatus.ACTIVE
        ));

        return repository;
    }

    private static void assertFlashItemState(
            FlashSaleItemRepository repository,
            int expectedSoldQty,
            int expectedVersion
    ) {

        FlashSaleItem item = repository.findById("FI_LOCK");

        assertNotNull(item);
        assertEquals(expectedSoldQty, item.getSoldQty());
        assertEquals(expectedVersion, item.getVersion());
    }

    private static void resetFile(String filePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(HEADER);
            writer.newLine();
        }
    }

    private interface SaleAction {
        boolean sell() throws Exception;
    }

    private static class ConcurrentResult {
        private final AtomicInteger successCount = new AtomicInteger();
        private final List<Throwable> expectedFailures =
                Collections.synchronizedList(new ArrayList<Throwable>());
        private final List<Throwable> unexpectedFailures =
                Collections.synchronizedList(new ArrayList<Throwable>());
    }
}
