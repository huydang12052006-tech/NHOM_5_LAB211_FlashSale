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

    public FlashSaleItemRepository() {
        super("data/flash_items.csv");
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

        FlashSaleItem item = findById( flashItemId);

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

        FlashSaleItem item = findById( flashItemId);

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
        FileLock khóa trực tiếp file CSV
        cấp OS level
         */
        try (
                RandomAccessFile raf
                = new RandomAccessFile(FILE_PATH, "rw"); FileChannel channel = raf.getChannel(); 
                FileLock lock = channel.lock()) {

            List<FlashSaleItem> items = findAll();

            FlashSaleItem item = findById( flashItemId);

            if (item == null) {
                return false;
            }

            if (item.getSoldQty() + quantity > item.getLimitedQty()) {
                throw new OutOfStockException("Not enough stock for " + flashItemId);
            }

            item.setSoldQty(item.getSoldQty() + quantity);

            rewriteFile(items);

            return true;
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

            FlashSaleItem latestItem
                    = findById(  flashItemId);

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
}
