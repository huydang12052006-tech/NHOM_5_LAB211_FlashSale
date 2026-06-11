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
}
    

    
