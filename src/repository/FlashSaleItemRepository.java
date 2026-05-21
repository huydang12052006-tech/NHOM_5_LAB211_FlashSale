package repository;

import model.entity.FlashSaleItem;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class FlashSaleItemRepository {
    
    private static final String FILE_PATH = "data/flash_items.csv";
    private static final int MAX_RETRY = 3;

    public boolean sellWithNoLock(String flashItemId,
            int quantity) throws IOException {

        List<FlashSaleItem> items = readAll();

        FlashSaleItem item = findById(items, flashItemId);

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

            writeAll(items);

            return true;
        }

        return false;
    }

    public synchronized boolean sellWithSynchronized(String flashItemId,
            int quantity)
            throws IOException {

        List<FlashSaleItem> items = readAll();

        FlashSaleItem item = findById(items, flashItemId);

        if (item == null) {
            return false;
        }

        /*
        synchronized đảm bảo:
        chỉ 1 thread được vào critical section
        tại cùng thời điểm
         */
        if (item.getSoldQty() + quantity > item.getLimitedQty()) {
            return false;
        }

        item.setSoldQty(item.getSoldQty() + quantity);

        writeAll(items);

        return true;
    }

    public boolean sellWithFileLock(String flashItemId,
            int quantity)
            throws IOException {

        Path path = Paths.get(FILE_PATH);

        /*
        FileLock khóa trực tiếp file CSV
        cấp OS level
         */
        try (
                RandomAccessFile raf
                = new RandomAccessFile(FILE_PATH, "rw"); FileChannel channel = raf.getChannel(); 
                FileLock lock = channel.lock()) {

            List<FlashSaleItem> items = readAll();

            FlashSaleItem item = findById(items, flashItemId);

            if (item == null) {
                return false;
            }

            if (item.getSoldQty() + quantity > item.getLimitedQty()) {
                return false;
            }

            item.setSoldQty(item.getSoldQty() + quantity);

            writeAll(items);

            return true;
        }
    }

    public boolean sellWithOptimisticLock(String flashItemId,
            int quantity)
            throws IOException {

        int retry = 0;

        while (retry < MAX_RETRY) {

            List<FlashSaleItem> items = readAll();

            FlashSaleItem item = findById(items, flashItemId);

            if (item == null) {
                return false;
            }

            int currentVersion = item.getVersion();

            int currentSold = item.getSoldQty();

            /*
            validate stock
             */
            if (currentSold + quantity > item.getLimitedQty()) {
                return false;
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
            List<FlashSaleItem> latestItems = readAll();

            FlashSaleItem latestItem
                    = findById(latestItems, flashItemId);

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

            writeAll(latestItems);

            return true;
        }

        return false;
    }
}
