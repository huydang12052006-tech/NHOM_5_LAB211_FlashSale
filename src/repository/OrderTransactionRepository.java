package repository;

import model.Entity.OrderTransaction;

public class OrderTransactionRepository extends CsvRepository<OrderTransaction> {

    public OrderTransactionRepository() {
        super("data/transactions.csv");
    }

    public OrderTransactionRepository(String filePath) {
        super(filePath);
    }
    // --- Quản lý Event ---

    @Override
    protected OrderTransaction mapFromCsv(String csvLine) {

        OrderTransaction orderTransaction = new OrderTransaction();

        try {
            orderTransaction.fromCsvLine(csvLine);
            return orderTransaction;
        } catch (Exception e) {
            return null;
        }
    }
}
    

    
