package repository;

import model.Entity.OrderDetail;


public class OrderDetailRepository extends CsvRepository<OrderDetail> {

    public OrderDetailRepository() {
        super("data/order_details.csv");
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

//     return total;
// }
}
    

    
