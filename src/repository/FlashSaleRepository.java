package repository;

import model.Entity.FlashSaleEvent;
import model.Entity.FlashSaleItem;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FlashSaleRepository {

    private final String eventFilePath = "data/flash_events.csv";
    private final String itemFilePath = "data/flash_items.csv";

    // --- Quản lý Event ---
    public List<FlashSaleEvent> findAllEvents() {
        List<FlashSaleEvent> list = new ArrayList<>();
        File file = new File(eventFilePath);
        if (!file.exists()) {
            return list;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                try {
                    list.add(FlashSaleEvent.fromCsvLine(line));
                } catch (Exception e) {
                    continue;
                }
            }
        } catch (IOException e) {
            System.out.println("[Loi]: Khong doc duoc file event.");
        }
        return list;
    }

    public void saveEvent(FlashSaleEvent event) {
        List<FlashSaleEvent> list = findAllEvents();
        list.add(event);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(eventFilePath))) {
            for (FlashSaleEvent e : list) {
                bw.write(e.toCsvLine());
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("[Loi]: Khong the ghi file event.");
        }
    }

    // --- Quản lý FlashSaleItem ---
    public List<FlashSaleItem> findAllItems() {
        List<FlashSaleItem> list = new ArrayList<>();
        File file = new File(itemFilePath);
        if (!file.exists()) {
            return list;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                try {
                    list.add(FlashSaleItem.fromCsvLine(line));
                } catch (Exception e) {
                    continue;
                }
            }
        } catch (IOException e) {
            System.out.println("[Loi]: Khong doc duoc file flash item.");
        }
        return list;
    }

    public void saveItem(FlashSaleItem item) {
        List<FlashSaleItem> list = findAllItems();
        list.add(item);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(itemFilePath))) {
            for (FlashSaleItem i : list) {
                bw.write(i.toCsvLine());
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("[Loi]: Khong the ghi file flash item.");
        }
    }
}
