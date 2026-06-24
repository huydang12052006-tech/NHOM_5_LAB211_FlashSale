package test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.time.LocalDateTime;
import model.Entity.Customer;
import model.Entity.FlashSaleEvent;
import model.Entity.FlashSaleItem;
import model.Entity.Order;
import model.Entity.OrderDetail;
import model.Entity.OrderTransaction;
import model.Entity.Payment;
import model.Entity.Product;
import model.Entity.User;
import model.Enum.CustomerTier;
import model.Enum.LockMechanism;
import model.Enum.OrderStatus;
import model.Enum.PaymentMethod;
import model.Enum.SaleStatus;
import model.Enum.UserRole;
import org.junit.jupiter.api.Test;

public class CsvLineJUnitTest {

    private static final LocalDateTime CREATED_AT =
            LocalDateTime.of(2026, 1, 2, 3, 4, 5);
    private static final LocalDateTime UPDATED_AT =
            LocalDateTime.of(2026, 2, 3, 4, 5, 6);
    private static final double DELTA = 0.000001;

    @Test
    void customerToCsvLine() {
        Customer customer = new Customer(
                "C001", CREATED_AT, UPDATED_AT,
                "U001", "Nguyen, Van A", "0909123456", "a@example.com",
                CustomerTier.VIP, 12345.5, true);

        assertEquals(
                "C001,2026-01-02T03:04:05,2026-02-03T04:05:06,"
                        + "U001,Nguyen  Van A,0909123456,a@example.com,VIP,12345.5,true",
                customer.toCsvLine()
        );
    }

    @Test
    void customerFromCsvLine() {
        Customer customer = new Customer();
        customer.fromCsvLine(
                "C002,2026-01-02T03:04:05,2026-02-03T04:05:06,"
                        + "U002,Tran Van B,0911222333,b@example.com,PREMIUM,999.75,false");

        assertBaseFields(customer, "C002");
        assertEquals("U002", customer.getUserId());
        assertEquals("Tran Van B", customer.getFullName());
        assertEquals("0911222333", customer.getPhone());
        assertEquals("b@example.com", customer.getEmail());
        assertEquals(CustomerTier.PREMIUM, customer.getTier());
        assertEquals(999.75, customer.getTotalSpent(), DELTA);
        assertFalse(customer.isActive());
    }

    @Test
    void productToCsvLine() {
        Product product = new Product(
                "P001", CREATED_AT, UPDATED_AT,
                "Laptop Pro", "Electronics", 1500.0, 25, 3,
                SaleStatus.ACTIVE, "U02501");

        assertEquals(
                "P001,2026-01-02T03:04:05,2026-02-03T04:05:06,"
                        + "Laptop Pro,Electronics,1500.0,25,3,ACTIVE,U02501",
                product.toCsvLine()
        );
    }

    @Test
    void productFromCsvLine() {
        Product product = new Product();
        product.fromCsvLine(
                "P002,2026-01-02T03:04:05,2026-02-03T04:05:06,"
                        + "Camera,Photo,899.99,10,4,DISABLED,U02502");

        assertBaseFields(product, "P002");
        assertEquals("Camera", product.getName());
        assertEquals("Photo", product.getCategory());
        assertEquals(899.99, product.getOriginalPrice(), DELTA);
        assertEquals(10, product.getStockQty());
        assertEquals(4, product.getVersion());
        assertEquals(SaleStatus.DISABLED, product.getStatus());
        assertEquals("U02502", product.getSellerId());
    }

    @Test
    void flashSaleEventToCsvLine() {
        FlashSaleEvent event = new FlashSaleEvent(
                "E001", CREATED_AT, UPDATED_AT,
                "Summer Sale",
                LocalDateTime.of(2026, 3, 1, 9, 0, 0),
                LocalDateTime.of(2026, 3, 1, 18, 0, 0),
                SaleStatus.UPCOMING);

        assertEquals(
                "E001,2026-01-02T03:04:05,2026-02-03T04:05:06,"
                        + "Summer Sale,2026-03-01T09:00,2026-03-01T18:00,UPCOMING",
                event.toCsvLine()
        );
    }

    @Test
    void flashSaleEventFromCsvLine() {
        FlashSaleEvent event = new FlashSaleEvent();
        event.fromCsvLine(
                "E002,2026-01-02T03:04:05,2026-02-03T04:05:06,"
                        + "New Year Sale,2026-01-10T08:30,2026-01-10T12:30,ACTIVE");

        assertBaseFields(event, "E002");
        assertEquals("New Year Sale", event.getEventName());
        assertEquals(LocalDateTime.of(2026, 1, 10, 8, 30), event.getStartTime());
        assertEquals(LocalDateTime.of(2026, 1, 10, 12, 30), event.getEndTime());
        assertEquals(SaleStatus.ACTIVE, event.getStatus());
    }

    @Test
    void flashSaleItemToCsvLine() {
        FlashSaleItem item = new FlashSaleItem(
                "FI001", CREATED_AT, UPDATED_AT,
                "E001", "P001", 1200.0, 50, 12, 20.0, 2,
                SaleStatus.ACTIVE);

        assertEquals(
                "FI001,2026-01-02T03:04:05,2026-02-03T04:05:06,"
                        + "E001,P001,1200.0,50,12,20.0,2,ACTIVE",
                item.toCsvLine()
        );
    }

    @Test
    void flashSaleItemFromCsvLine() {
        FlashSaleItem item = new FlashSaleItem();
        item.fromCsvLine(
                "FI002,2026-01-02T03:04:05,2026-02-03T04:05:06,"
                        + "E002,P002,499.5,30,5,33.5,7,ENDED");

        assertBaseFields(item, "FI002");
        assertEquals("E002", item.getEventId());
        assertEquals("P002", item.getProductId());
        assertEquals(499.5, item.getFlashPrice(), DELTA);
        assertEquals(30, item.getLimitedQty());
        assertEquals(5, item.getSoldQty());
        assertEquals(33.5, item.getDiscountPercent(), DELTA);
        assertEquals(7, item.getVersion());
        assertEquals(SaleStatus.ENDED, item.getStatus());
    }

    @Test
    void orderToCsvLine() {
        Order order = new Order(
                "O001", CREATED_AT, UPDATED_AT,
                "C001", "E001", 2400.0,
                OrderStatus.SUCCESS, LockMechanism.OPTIMISTIC_LOCK);

        assertEquals(
                "O001,2026-01-02T03:04:05,2026-02-03T04:05:06,"
                        + "C001,E001,2400.0,SUCCESS,OPTIMISTIC_LOCK",
                order.toCsvLine()
        );
    }

    @Test
    void orderFromCsvLine() {
        Order order = new Order();
        order.fromCsvLine(
                "O002,2026-01-02T03:04:05,2026-02-03T04:05:06,"
                        + "C002,E002,150.25,FAILED,SYNCHRONIZED");

        assertBaseFields(order, "O002");
        assertEquals("C002", order.getCustomerId());
        assertEquals("E002", order.getEventId());
        assertEquals(150.25, order.getTotalAmount(), DELTA);
        assertEquals(OrderStatus.FAILED, order.getStatus());
        assertEquals(LockMechanism.SYNCHRONIZED, order.getLockMechanism());
    }

    @Test
    void orderDetailToCsvLine() {
        OrderDetail detail = new OrderDetail(
                "OD001", CREATED_AT, UPDATED_AT,
                "O001", "FI001", "P001", 2, 1200.0, 2400.0);

        assertEquals(
                "OD001,2026-01-02T03:04:05,2026-02-03T04:05:06,"
                        + "O001,FI001,P001,2,1200.0,2400.0",
                detail.toCsvLine()
        );
    }

    @Test
    void orderDetailFromCsvLine() {
        OrderDetail detail = new OrderDetail();
        detail.fromCsvLine(
                "OD002,2026-01-02T03:04:05,2026-02-03T04:05:06,"
                        + "O002,FI002,P002,3,99.9,299.7");

        assertBaseFields(detail, "OD002");
        assertEquals("O002", detail.getOrderId());
        assertEquals("FI002", detail.getFlashItemId());
        assertEquals("P002", detail.getProductId());
        assertEquals(3, detail.getQuantity());
        assertEquals(99.9, detail.getUnitPrice(), DELTA);
        assertEquals(299.7, detail.getSubTotal(), DELTA);
    }

    @Test
    void regularOrderUsesEmptyEventAndFlashItem() {
        Order order = new Order("O003", CREATED_AT, UPDATED_AT, "C003", null,
                500.0, OrderStatus.PENDING, LockMechanism.NO_LOCK);
        OrderDetail detail = new OrderDetail("OD003", CREATED_AT, UPDATED_AT,
                "O003", null, "P003", 1, 500.0, 500.0);

        assertEquals("O003,2026-01-02T03:04:05,2026-02-03T04:05:06,C003,,500.0,PENDING,NO_LOCK",
                order.toCsvLine());
        assertEquals("OD003,2026-01-02T03:04:05,2026-02-03T04:05:06,O003,,P003,1,500.0,500.0",
                detail.toCsvLine());
    }

    @Test
    void orderTransactionToCsvLine() {
        OrderTransaction transaction = new OrderTransaction(
                "T001", CREATED_AT, UPDATED_AT,
                "O001", "worker-1", LockMechanism.FILE_LOCK,
                true, 1, 35L, "ok, completed");

        assertEquals(
                "T001,2026-01-02T03:04:05,2026-02-03T04:05:06,"
                        + "O001,worker-1,FILE_LOCK,true,1,35,ok  completed",
                transaction.toCsvLine()
        );
    }

    @Test
    void orderTransactionFromCsvLine() {
        OrderTransaction transaction = new OrderTransaction();
        transaction.fromCsvLine(
                "T002,2026-01-02T03:04:05,2026-02-03T04:05:06,"
                        + "O002,worker-2,NO_LOCK,false,3,88,failed");

        assertBaseFields(transaction, "T002");
        assertEquals("O002", transaction.getOrderId());
        assertEquals("worker-2", transaction.getThreadName());
        assertEquals(LockMechanism.NO_LOCK, transaction.getMechanism());
        assertFalse(transaction.isSuccess());
        assertEquals(3, transaction.getRetryCount());
        assertEquals(88L, transaction.getExecutionTimeMs());
        assertEquals("failed", transaction.getMessage());
    }

    @Test
    void paymentToCsvLine() {
        Payment payment = new Payment(
                "PAY001", CREATED_AT, UPDATED_AT,
                "O001", "C001", PaymentMethod.CASH, 12345.0);

        assertEquals(
                "PAY001,2026-01-02T03:04:05,2026-02-03T04:05:06,"
                        + "O001,C001,CASH,12345.0",
                payment.toCsvLine()
        );
    }

    @Test
    void paymentFromCsvLine() {
        Payment payment = new Payment();
        payment.fromCsvLine(
                "PAY002,2026-01-02T03:04:05,2026-02-03T04:05:06,"
                        + "O002,C002,BANKING,999.5");

        assertBaseFields(payment, "PAY002");
        assertEquals("O002", payment.getOrderId());
        assertEquals("C002", payment.getCustomerId());
        assertEquals(PaymentMethod.BANKING, payment.getPaymentMethod());
        assertEquals(999.5, payment.getAmount(), DELTA);
    }

    @Test
    void userToCsvLine() {
        User user = new User(
                "U001", CREATED_AT, UPDATED_AT,
                "user1", "$2a$10$hashvalue", UserRole.CUSTOMER, true);

        assertEquals(
                "U001,2026-01-02T03:04:05,2026-02-03T04:05:06,"
                        + "user1,$2a$10$hashvalue,CUSTOMER,true",
                user.toCsvLine()
        );
    }

    @Test
    void userFromCsvLine() {
        User user = new User();
        user.fromCsvLine(
                "U002,2026-01-02T03:04:05,2026-02-03T04:05:06,"
                        + "user2,$2a$10$abc,ADMIN,false");

        assertBaseFields(user, "U002");
        assertEquals("user2", user.getUsername());
        assertEquals("$2a$10$abc", user.getPasswordHash());
        assertEquals(UserRole.ADMIN, user.getRole());
        assertFalse(user.isActive());
    }

    private static void assertBaseFields(
            model.BaseEntity.BaseEntity entity,
            String expectedId
    ) {
        assertEquals(expectedId, entity.getId());
        assertEquals(CREATED_AT, entity.getCreatedAt());
        assertEquals(UPDATED_AT, entity.getUpdatedAt());
    }
}
