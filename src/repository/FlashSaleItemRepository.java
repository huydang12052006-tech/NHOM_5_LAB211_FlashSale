package repository;

import model.Entity.FlashSaleItem;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

import java.util.List;

import exception.*;

public class FlashSaleItemRepository extends CsvRepository<FlashSaleItem> {
    
    private static final String FILE_PATH = "data/flash_items.csv";
    private static final int MAX_RETRY = 3;
    private final String lockFilePath;

    public FlashSaleItemRepository() {
        this(FILE_PATH);
    }

    public FlashSaleItemRepository(String filePath) {
        super(filePath);
        this.lockFilePath = filePath;
    }
    // --- Quản lý Item ---

    @Override
    protected FlashSaleItem mapFromCsv(String csvLine) {

        FlashSaleItem item = new FlashSaleItem();

        try {
            item.fromCsvLine(csvLine);
            return item;
        } catch (Exception e) {
            return null;
        }
    }

    public boolean sellWithNoLock(String flashItemId,
            int quantity) throws IOException, FlashSaleException {

        List<FlashSaleItem> items = findAll();

        FlashSaleItem item = findInList(items, flashItemId);

        if (item == null) {
            return false;
        }

        /*
        RACE WINDOW

        Thread A đọc soldQty = 8
        Thread B đọc soldQty = 8

        limitedQty = 10

        cả hai đều thấy còn hàng
        cả hai cùng update

        kết quả:
        soldQty = 12
         */
        if (item.getSoldQty() + quantity <= item.getLimitedQty()) {

            // giả lập delay I/O để race condition dễ xảy ra
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            item.setSoldQty(item.getSoldQty() + quantity);

            rewriteFile(items);

            return true;
        }

        throw new OutOfStockException("Not enough stock for " + flashItemId);
    }

    public synchronized boolean sellWithSynchronized(String flashItemId,
            int quantity)
            throws IOException, FlashSaleException {

        List<FlashSaleItem> items = findAll();

        FlashSaleItem item = findInList(items, flashItemId);

        if (item == null) {
            return false;
        }

        /*
        synchronized đảm bảo:
        chỉ 1 thread được vào critical section
        tại cùng thời điểm
         */
        if (item.getSoldQty() + quantity > item.getLimitedQty()) {
            throw new OutOfStockException("Not enough stock for " + flashItemId);
        }

        item.setSoldQty(item.getSoldQty() + quantity);

        rewriteFile(items);

        return true;
    }

    public boolean sellWithFileLock(String flashItemId,
            int quantity)
            throws IOException, FlashSaleException {

        /*
        Acquire an OS-level file lock and perform read/update/write while
        holding the lock to avoid external writers causing IO exceptions on Windows.
         */
        RandomAccessFile raf = null;
        FileChannel channel = null;
        FileLock lock = null;

        try {
            raf = new RandomAccessFile(lockFilePath, "rw");
            channel = raf.getChannel();
            lock = channel.lock();

            // Read all lines using the opened file descriptor while holding the lock
            List<FlashSaleItem> items = new java.util.ArrayList<>();

            raf.seek(0);
            String line;
            while ((line = raf.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                if (line.startsWith("id,")) continue; // header

                try {
                    FlashSaleItem it = new FlashSaleItem();
                    it.fromCsvLine(line);
                    items.add(it);
                } catch (Exception e) {
                    // skip malformed lines
                }
            }

            FlashSaleItem target = null;
            for (FlashSaleItem it : items) {
                if (it.getId().equals(flashItemId)) {
                    target = it;
                    break;
                }
            }

            if (target == null) {
                return false;
            }

            if (target.getSoldQty() + quantity > target.getLimitedQty()) {
                throw new OutOfStockException("Not enough stock for " + flashItemId);
            }

            target.setSoldQty(target.getSoldQty() + quantity);

            // rewrite file while still holding the lock
            raf.setLength(0);
            raf.seek(0);

            raf.writeBytes("id,createdAt,updatedAt,eventId,productId,flashPrice,limitedQty,soldQty,discountPercent,version,status");
            raf.writeBytes(System.lineSeparator());

            for (FlashSaleItem it : items) {
                raf.writeBytes(it.toCsvLine());
                raf.writeBytes(System.lineSeparator());
            }

            return true;
        } finally {
            try {
                if (lock != null && lock.isValid()) lock.release();
            } catch (IOException ignored) {}
            try {
                if (channel != null) channel.close();
            } catch (IOException ignored) {}
            try {
                if (raf != null) raf.close();
            } catch (IOException ignored) {}
        }
    }

    public boolean sellWithOptimisticLock(String flashItemId,
            int quantity)
            throws IOException, FlashSaleException {

        int retry = 0;

        while (retry < MAX_RETRY) {


            FlashSaleItem item = findById( flashItemId);

            if (item == null) {
                return false;
            }

            int currentVersion = item.getVersion();

            int currentSold = item.getSoldQty();

            /*
            validate stock
             */
            if (currentSold + quantity > item.getLimitedQty()) {
                throw new OutOfStockException("Not enough stock for " + flashItemId);
            }

            /*
            giả lập race window
             */
            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            /*
            RE-READ latest data
             */
            List<FlashSaleItem> latestItems = findAll();

            FlashSaleItem latestItem = findInList(latestItems, flashItemId);

            /*
            If latestItem is missing (due to transient file parse errors),
            treat as version conflict and retry.
             */
            if (latestItem == null) {
                retry++;
                System.out.println(Thread.currentThread().getName()
                        + " latestItem=null -> retry " + retry);
                continue;
            }

            /*
            version changed
            => thread khác đã update trước
             */
            if (latestItem.getVersion() != currentVersion) {
                retry++;

                System.out.println(
                        Thread.currentThread().getName()
                        + " version conflict -> retry "
                        + retry
                );

                continue;
            }

            /*
            version vẫn giống
            => update an toàn
             */
            latestItem.setSoldQty(
                    latestItem.getSoldQty() + quantity
            );

            latestItem.setVersion(
                    latestItem.getVersion() + 1
            );

            rewriteFile(latestItems);

            return true;
        }

        // nếu retry vượt mức, báo lỗi rõ ràng
        throw new VersionConflictException("Optimistic lock failed after " + MAX_RETRY + " retries for " + flashItemId);
    }

    /**
     * Generates the next flash item ID based on the max existing FIxxxxx.
     */
    public String generateNextId() {
        int maxNumber = 0;

        for (FlashSaleItem item : findAll()) {
            String id = item.getId();
            if (id != null && id.matches("FI\\d+")) {
                maxNumber = Math.max(maxNumber, Integer.parseInt(id.substring(2)));
            }
        }

        return String.format("FI%05d", maxNumber + 1);
    }

    private FlashSaleItem findInList(List<FlashSaleItem> items, String flashItemId) {
        for (FlashSaleItem item : items) {
            if (item.getId().equals(flashItemId)) {
                return item;
            }
        }

        return null;
    }
}

