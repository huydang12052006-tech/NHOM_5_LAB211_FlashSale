package test;

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

public class FileLockJUnitTest {

    private static final Path TEST_DIR =
            Paths.get("data", "file_lock_junit_test");
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
    void testOnePersonOrdersOneProduct() throws Exception {
        FlashSaleItemRepository repository =
                prepareRepository("file_lock_single.csv", 2, 10, 1);

        assertTrue(repository.sellWithFileLock("FI_LOCK", 1));

        FlashSaleItem item = repository.findById("FI_LOCK");
        assertNotNull(item);
        assertEquals(3, item.getSoldQty());
        assertEquals(1, item.getVersion());
    }

    @Test
    void testOnePersonOrdersExceedingQuantity() throws Exception {
        FlashSaleItemRepository repository =
                prepareRepository("file_lock_exceed.csv", 2, 10, 1);

        assertThrows(
                OutOfStockException.class,
                () -> repository.sellWithFileLock("FI_LOCK", 9)
        );

        FlashSaleItem item = repository.findById("FI_LOCK");
        assertNotNull(item);
        assertEquals(2, item.getSoldQty());
        assertEquals(1, item.getVersion());
    }

    @Test
    void testTwoPeopleConcurrentlyOrderLastProduct() throws Exception {
        // limit = 1, sold = 0 -> only 1 remaining.
        // Both try to buy 1 item.
        FlashSaleItemRepository repository =
                prepareRepository("file_lock_concurrent.csv", 0, 1, 1);

        ConcurrentResult result = runTwoConcurrentSales(
                new SaleAction() {
                    @Override
                    public boolean sell() throws Exception {
                        return repository.sellWithFileLock("FI_LOCK", 1);
                    }
                }
        );

        // With file lock, only one thread can succeed.
        // The other thread will fail either with OutOfStockException (if it executes after the first thread releases the lock)
        // or OverlappingFileLockException (if it tries to lock concurrently).
        assertTrue(result.unexpectedFailures.isEmpty(), "Unexpected failures: " + result.unexpectedFailures);
        assertEquals(1, result.successCount.get());
        assertEquals(1, result.expectedFailures.size());
        Throwable failure = result.expectedFailures.get(0);
        assertTrue(failure instanceof OutOfStockException || failure instanceof java.nio.channels.OverlappingFileLockException,
                   "Expected failure to be OutOfStockException or OverlappingFileLockException, but was: " + failure);
        
        FlashSaleItem item = repository.findById("FI_LOCK");
        assertNotNull(item);
        assertEquals(1, item.getSoldQty());
        assertEquals(1, item.getVersion());
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
                } catch (java.nio.channels.OverlappingFileLockException e) {
                    result.expectedFailures.add(e);
                } catch (Throwable e) {
                    result.unexpectedFailures.add(e);
                } finally {
                    done.countDown();
                }
            }
        };

        new Thread(task, "file-lock-test-1").start();
        new Thread(task, "file-lock-test-2").start();

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
