package repository;

import model.Entity.FlashSaleEvent;


public class FlashSaleRepository extends CsvRepository<FlashSaleEvent> {

    public FlashSaleRepository() {
        super("data/flash_events.csv");
    }

    public FlashSaleRepository(String filePath) {
        super(filePath);
    }
    // --- Quản lý Event ---

    @Override
    protected FlashSaleEvent mapFromCsv(String csvLine) {

        FlashSaleEvent event = new FlashSaleEvent();

        try {
            event.fromCsvLine(csvLine);
            return event;
        } catch (Exception e) {
            return null;
        }
    }

    public String generateNextId() {
        int maxNumber = 0;
        for (FlashSaleEvent event : findAll()) {
            String id = event.getId();
            if (id != null && id.matches("E\\d+")) {
                maxNumber = Math.max(maxNumber, Integer.parseInt(id.substring(1)));
            }
        }
        return String.format("E%03d", maxNumber + 1);
    }
}
    

    
