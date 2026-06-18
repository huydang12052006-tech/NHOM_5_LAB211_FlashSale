package repository;

import model.Entity.OrderDetail;


public class OrderDetailRepository extends CsvRepository<OrderDetail> {

    public OrderDetailRepository() {
        super("data/order_details.csv");
    }

    public OrderDetailRepository(String filePath) {
        super(filePath);
    }
    // --- Quản lý Event ---

    @Override
    protected OrderDetail mapFromCsv(String csvLine) {

        OrderDetail orderDetail = new OrderDetail();

        try {
            orderDetail.fromCsvLine(csvLine);
            return orderDetail;
        } catch (Exception e) {
            return null;
        }
    }

//     public int getPurchasedQuantity(String customerId,
//                                 String flashItemId) {

//     int total = 0;

//     try {

//         List<Order> orders = findAll();

//         OrderDetailRepository detailRepo =
//                 new OrderDetailRepository();

//         for (Order order : orders) {

//             if (!customerId.equals(
//                     order.getCustomerId())) {

//                 continue;
//             }

//             List<OrderDetail> details =
//                     detailRepo.findByOrderId(
//                             order.getId()
//                     );

//             for (OrderDetail detail : details) {

//                 if (flashItemId.equals(
//                         detail.getFlashSaleItemId())) {

//                     total += detail.getQuantity();
//                 }
//             }
//         }

//     } catch (Exception e) {

//         System.out.println(
//                 "[ERROR] " + e.getMessage()
//         );
//     }

    public String generateNextId() {
        int maxNumber = 0;
        for (OrderDetail detail : findAll()) {
            String id = detail.getId();
            if (id != null && id.matches("OD\\d+")) {
                maxNumber = Math.max(maxNumber, Integer.parseInt(id.substring(2)));
            }
        }
        return String.format("OD%06d", maxNumber + 1);
    }
}
    

    
