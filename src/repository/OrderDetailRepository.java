package repository;

import model.Entity.Order;


public class OrderRepository extends CsvRepository<Order> {

    public OrderRepository() {
        super("data/orders.csv");
    }
    // --- Quản lý Event ---

    @Override
    protected Order mapFromCsv(String csvLine) {

        Order order = new Order();

        try {
            order.fromCsvLine(csvLine);
            return order;
        } catch (Exception e) {
            return null;
        }
    }
}
    

    
