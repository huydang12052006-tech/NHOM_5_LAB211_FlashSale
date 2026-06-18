package repository;

import java.util.List;

import model.Entity.Order;
import model.Entity.OrderDetail;
import java.util.ArrayList;



public class OrderRepository extends CsvRepository<Order> {

    public OrderRepository() {
        super("data/orders.csv");
    }

    public OrderRepository(String filePath) {
        super(filePath);
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

    public int getPurchasedQuantity(String customerId,
                                String flashItemId) {

    int total = 0;

    try {

        List<Order> orders = findAll();

        OrderDetailRepository detailRepo =
                new OrderDetailRepository();

        for (Order order : orders) {

            if (!customerId.equals(
                    order.getCustomerId())) {

                continue;
            }

            // List<OrderDetail> details =
            //         detailRepo.findByOrderId(
            //                 order.getId()
            //         );
            List<OrderDetail> details = new ArrayList<>();
                for (OrderDetail detail : detailRepo.findAll()) {
    
                    if (order.getId().equals(
                            detail.getOrderId())) {
    
                        details.add(detail);
                    }
                }
            

            for (OrderDetail detail : details) {

                if (flashItemId.equals(
                        detail.getFlashItemId())) {

                    total += detail.getQuantity();
                }
            }
        }

    } catch (Exception e) {

        System.out.println(
                "[ERROR] " + e.getMessage()
        );
    }
    return total;
    }

    public String generateNextId() {
        int maxNumber = 0;
        for (Order order : findAll()) {
            String id = order.getId();
            if (id != null && id.matches("O\\d+")) {
                maxNumber = Math.max(maxNumber, Integer.parseInt(id.substring(1)));
            }
        }
        return String.format("O%06d", maxNumber + 1);
    }
}
    

    
