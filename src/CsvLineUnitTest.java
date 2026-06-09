import java.time.LocalDateTime;

import model.Entity.Customer;
import model.Entity.FlashSaleEvent;
import model.Entity.FlashSaleItem;
import model.Entity.Order;
import model.Entity.OrderDetail;
import model.Entity.OrderTransaction;
import model.Entity.Product;
import model.Entity.Payment;
import model.Entity.User;
import model.Enum.CustomerTier;
import model.Enum.LockMechanism;
import model.Enum.OrderStatus;
import model.Enum.SaleStatus;
import model.Enum.PaymentMethod;
import model.Enum.UserRole;

public class CsvLineUnitTest {

    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 1, 2, 3, 4, 5);
    private static final LocalDateTime UPDATED_AT = LocalDateTime.of(2026, 2, 3, 4, 5, 6);
    private static int totalTests = 0;
    private static int passedTests = 0;

    public static void main(String[] args) {
        runTest("Customer.toCsvLine", CsvLineUnitTest::testCustomerToCsvLine);
        runTest("Customer.fromCsvLine", CsvLineUnitTest::testCustomerFromCsvLine);
        runTest("Product.toCsvLine", CsvLineUnitTest::testProductToCsvLine);
        runTest("Product.fromCsvLine", CsvLineUnitTest::testProductFromCsvLine);
        runTest("FlashSaleEvent.toCsvLine", CsvLineUnitTest::testFlashSaleEventToCsvLine);
        runTest("FlashSaleEvent.fromCsvLine", CsvLineUnitTest::testFlashSaleEventFromCsvLine);
        runTest("FlashSaleItem.toCsvLine", CsvLineUnitTest::testFlashSaleItemToCsvLine);
        runTest("FlashSaleItem.fromCsvLine", CsvLineUnitTest::testFlashSaleItemFromCsvLine);
        runTest("Order.toCsvLine", CsvLineUnitTest::testOrderToCsvLine);
        runTest("Order.fromCsvLine", CsvLineUnitTest::testOrderFromCsvLine);
        runTest("OrderDetail.toCsvLine", CsvLineUnitTest::testOrderDetailToCsvLine);
        runTest("OrderDetail.fromCsvLine", CsvLineUnitTest::testOrderDetailFromCsvLine);
        runTest("OrderTransaction.toCsvLine", CsvLineUnitTest::testOrderTransactionToCsvLine);
        runTest("OrderTransaction.fromCsvLine", CsvLineUnitTest::testOrderTransactionFromCsvLine);
        runTest("Payment.toCsvLine", CsvLineUnitTest::testPaymentToCsvLine);
        runTest("Payment.fromCsvLine", CsvLineUnitTest::testPaymentFromCsvLine);
        runTest("User.toCsvLine", CsvLineUnitTest::testUserToCsvLine);
        runTest("User.fromCsvLine", CsvLineUnitTest::testUserFromCsvLine);

        System.out.println("========================================");
        System.out.println("SUMMARY: " + passedTests + "/" + totalTests + " tests passed.");

        if (passedTests != totalTests) {
            throw new AssertionError("Some CSV line unit tests failed.");
        }
    }

    private static void testCustomerToCsvLine() {
        Customer customer = new Customer(
                "C001", CREATED_AT, UPDATED_AT,
                "Nguyen, Van A", "0909123456", "a@example.com",
                CustomerTier.VIP, 12345.5, true);

        String expected = "C001,2026-01-02T03:04:05,2026-02-03T04:05:06,Nguyen  Van A,0909123456,a@example.com,VIP,12345.5,true";
        String actual = customer.toCsvLine();

        printDebug("Input object", customer.toString(), expected, actual);
        assertEquals(expected, actual);
    }

    private static void testCustomerFromCsvLine() {
        Customer customer = new Customer();
        String input = "C002,2026-01-02T03:04:05,2026-02-03T04:05:06,Tran Van B,0911222333,b@example.com,PREMIUM,999.75,false";
        customer.fromCsvLine(input);

        printDebug("Input CSV", input, input, customer.toCsvLine());
        assertBaseFields(customer, "C002");
        assertEquals("Tran Van B", customer.getFullName());
        assertEquals("0911222333", customer.getPhone());
        assertEquals("b@example.com", customer.getEmail());
        assertEquals(CustomerTier.PREMIUM, customer.getTier());
        assertEquals(999.75, customer.getTotalSpent());
        assertEquals(false, customer.isActive());
    }

    private static void testProductToCsvLine() {
        Product product = new Product(
                "P001", CREATED_AT, UPDATED_AT,
                "Laptop Pro", "Electronics", 1500.0, 25, 3,
                SaleStatus.ACTIVE);

        String expected = "P001,2026-01-02T03:04:05,2026-02-03T04:05:06,Laptop Pro,Electronics,1500.0,25,3,ACTIVE";
        String actual = product.toCsvLine();

        printDebug("Input object", describeProduct(product), expected, actual);
        assertEquals(expected, actual);
    }

    private static void testProductFromCsvLine() {
        Product product = new Product();
        String input = "P002,2026-01-02T03:04:05,2026-02-03T04:05:06,Camera,Photo,899.99,10,4,DISABLED";
        product.fromCsvLine(input);

        printDebug("Input CSV", input, input, product.toCsvLine());
        assertBaseFields(product, "P002");
        assertEquals("Camera", product.getName());
        assertEquals("Photo", product.getCategory());
        assertEquals(899.99, product.getOriginalPrice());
        assertEquals(10, product.getStockQty());
        assertEquals(4, product.getVersion());
        assertEquals(SaleStatus.DISABLED, product.getStatus());
    }

    private static void testFlashSaleEventToCsvLine() {
        FlashSaleEvent event = new FlashSaleEvent(
                "E001", CREATED_AT, UPDATED_AT,
                "Summer Sale",
                LocalDateTime.of(2026, 3, 1, 9, 0, 0),
                LocalDateTime.of(2026, 3, 1, 18, 0, 0),
                SaleStatus.UPCOMING);

        String expected = "E001,2026-01-02T03:04:05,2026-02-03T04:05:06,Summer Sale,2026-03-01T09:00,2026-03-01T18:00,UPCOMING";
        String actual = event.toCsvLine();

        printDebug("Input object", describeFlashSaleEvent(event), expected, actual);
        assertEquals(expected, actual);
    }

    private static void testFlashSaleEventFromCsvLine() {
        FlashSaleEvent event = new FlashSaleEvent();
        String input = "E002,2026-01-02T03:04:05,2026-02-03T04:05:06,New Year Sale,2026-01-10T08:30,2026-01-10T12:30,ACTIVE";
        event.fromCsvLine(input);

        printDebug("Input CSV", input, input, event.toCsvLine());
        assertBaseFields(event, "E002");
        assertEquals("New Year Sale", event.getEventName());
        assertEquals(LocalDateTime.of(2026, 1, 10, 8, 30), event.getStartTime());
        assertEquals(LocalDateTime.of(2026, 1, 10, 12, 30), event.getEndTime());
        assertEquals(SaleStatus.ACTIVE, event.getStatus());
    }

    private static void testFlashSaleItemToCsvLine() {
        FlashSaleItem item = new FlashSaleItem(
                "FI001", CREATED_AT, UPDATED_AT,
                "E001", "P001", 1200.0, 50, 12, 20.0, 2,
                SaleStatus.ACTIVE);

        String expected = "FI001,2026-01-02T03:04:05,2026-02-03T04:05:06,E001,P001,1200.0,50,12,20.0,2,ACTIVE";
        String actual = item.toCsvLine();

        printDebug("Input object", describeFlashSaleItem(item), expected, actual);
        assertEquals(expected, actual);
    }

    private static void testFlashSaleItemFromCsvLine() {
        FlashSaleItem item = new FlashSaleItem();
        String input = "FI002,2026-01-02T03:04:05,2026-02-03T04:05:06,E002,P002,499.5,30,5,33.5,7,ENDED";
        item.fromCsvLine(input);

        printDebug("Input CSV", input, input, item.toCsvLine());
        assertBaseFields(item, "FI002");
        assertEquals("E002", item.getEventId());
        assertEquals("P002", item.getProductId());
        assertEquals(499.5, item.getFlashPrice());
        assertEquals(30, item.getLimitedQty());
        assertEquals(5, item.getSoldQty());
        assertEquals(33.5, item.getDiscountPercent());
        assertEquals(7, item.getVersion());
        assertEquals(SaleStatus.ENDED, item.getStatus());
    }

    private static void testOrderToCsvLine() {
        Order order = new Order(
                "O001", CREATED_AT, UPDATED_AT,
                "C001", "E001", 2400.0,
                OrderStatus.SUCCESS, LockMechanism.OPTIMISTIC_LOCK);

        String expected = "O001,2026-01-02T03:04:05,2026-02-03T04:05:06,C001,E001,2400.0,SUCCESS,OPTIMISTIC_LOCK";
        String actual = order.toCsvLine();

        printDebug("Input object", order.toString(), expected, actual);
        assertEquals(expected, actual);
    }

    private static void testOrderFromCsvLine() {
        Order order = new Order();
        String input = "O002,2026-01-02T03:04:05,2026-02-03T04:05:06,C002,E002,150.25,FAILED,SYNCHRONIZED";
        order.fromCsvLine(input);

        printDebug("Input CSV", input, input, order.toCsvLine());
        assertBaseFields(order, "O002");
        assertEquals("C002", order.getCustomerId());
        assertEquals("E002", order.getEventId());
        assertEquals(150.25, order.getTotalAmount());
        assertEquals(OrderStatus.FAILED, order.getStatus());
        assertEquals(LockMechanism.SYNCHRONIZED, order.getLockMechanism());
    }

    private static void testOrderDetailToCsvLine() {
        OrderDetail detail = new OrderDetail(
                "OD001", CREATED_AT, UPDATED_AT,
                "O001", "FI001", 2, 1200.0, 2400.0);

        String expected = "OD001,2026-01-02T03:04:05,2026-02-03T04:05:06,O001,FI001,2,1200.0,2400.0";
        String actual = detail.toCsvLine();

        printDebug("Input object", detail.toString(), expected, actual);
        assertEquals(expected, actual);
    }

    private static void testOrderDetailFromCsvLine() {
        OrderDetail detail = new OrderDetail();
        String input = "OD002,2026-01-02T03:04:05,2026-02-03T04:05:06,O002,FI002,3,99.9,299.7";
        detail.fromCsvLine(input);

        printDebug("Input CSV", input, input, detail.toCsvLine());
        assertBaseFields(detail, "OD002");
        assertEquals("O002", detail.getOrderId());
        assertEquals("FI002", detail.getFlashItemId());
        assertEquals(3, detail.getQuantity());
        assertEquals(99.9, detail.getUnitPrice());
        assertEquals(299.7, detail.getSubTotal());
    }

    private static void testOrderTransactionToCsvLine() {
        OrderTransaction transaction = new OrderTransaction(
                "T001", CREATED_AT, UPDATED_AT,
                "O001", "worker-1", LockMechanism.FILE_LOCK,
                true, 1, 35L, "ok, completed");

        String expected = "T001,2026-01-02T03:04:05,2026-02-03T04:05:06,O001,worker-1,FILE_LOCK,true,1,35,ok  completed";
        String actual = transaction.toCsvLine();

        printDebug("Input object", transaction.toString(), expected, actual);
        assertEquals(expected, actual);
    }

    private static void testOrderTransactionFromCsvLine() {
        OrderTransaction transaction = new OrderTransaction();
        String input = "T002,2026-01-02T03:04:05,2026-02-03T04:05:06,O002,worker-2,NO_LOCK,false,3,88,failed";
        transaction.fromCsvLine(input);

        printDebug("Input CSV", input, input, transaction.toCsvLine());
        assertBaseFields(transaction, "T002");
        assertEquals("O002", transaction.getOrderId());
        assertEquals("worker-2", transaction.getThreadName());
        assertEquals(LockMechanism.NO_LOCK, transaction.getMechanism());
        assertEquals(false, transaction.isSuccess());
        assertEquals(3, transaction.getRetryCount());
        assertEquals(88L, transaction.getExecutionTimeMs());
        assertEquals("failed", transaction.getMessage());
    }

    private static void testPaymentToCsvLine() {
        Payment payment = new Payment(
                "PAY001", CREATED_AT, UPDATED_AT,
                "O001", "C001", PaymentMethod.CASH, 12345.0);

        String expected = "PAY001,2026-01-02T03:04:05,2026-02-03T04:05:06,O001,C001,CASH,12345.0";
        String actual = payment.toCsvLine();

        printDebug("Input object", payment.toString(), expected, actual);
        assertEquals(expected, actual);
    }

    private static void testPaymentFromCsvLine() {
        Payment payment = new Payment();
        String input = "PAY002,2026-01-02T03:04:05,2026-02-03T04:05:06,O002,C002,BANKING,999.5";
        payment.fromCsvLine(input);

        printDebug("Input CSV", input, input, payment.toCsvLine());
        assertBaseFields(payment, "PAY002");
        assertEquals("O002", payment.getOrderId());
        assertEquals("C002", payment.getCustomerId());
        assertEquals(PaymentMethod.BANKING, payment.getPaymentMethod());
        assertEquals(999.5, payment.getAmount());
    }

    private static void testUserToCsvLine() {
        User user = new User(
                "U001", CREATED_AT, UPDATED_AT,
                "user1", "$2a$10$hashvalue", model.Enum.UserRole.CUSTOMER, true);

        String expected = "U001,2026-01-02T03:04:05,2026-02-03T04:05:06,user1,$2a$10$hashvalue,CUSTOMER,true";
        String actual = user.toCsvLine();

        printDebug("Input object", user.toString(), expected, actual);
        assertEquals(expected, actual);
    }

    private static void testUserFromCsvLine() {
        User user = new User();
        String input = "U002,2026-01-02T03:04:05,2026-02-03T04:05:06,user2,$2a$10$abc,ADMIN,false";
        user.fromCsvLine(input);

        printDebug("Input CSV", input, input, user.toCsvLine());
        assertBaseFields(user, "U002");
        assertEquals("user2", user.getUsername());
        assertEquals("$2a$10$abc", user.getPasswordHash());
        assertEquals(UserRole.ADMIN, user.getRole());
        assertEquals(false, user.isActive());
    }

    private static void runTest(String testName, Runnable testMethod) {
        totalTests++;
        System.out.println("========================================");
        System.out.println("TEST: " + testName);

        try {
            testMethod.run();
            passedTests++;
            System.out.println("RESULT: PASS");
        } catch (AssertionError error) {
            System.out.println("RESULT: FAIL");
            System.out.println("ERROR : " + error.getMessage());
        }
    }

    private static void printDebug(
            String inputLabel,
            String input,
            String expected,
            String actual) {

        System.out.println(inputLabel + ": " + input);
        System.out.println("Expected   : " + expected);
        System.out.println("Actual     : " + actual);
    }

    private static String describeProduct(Product product) {
        return "Product{id='" + product.getId() + '\'' +
                ", createdAt=" + product.getCreatedAt() +
                ", updatedAt=" + product.getUpdatedAt() +
                ", name='" + product.getName() + '\'' +
                ", category='" + product.getCategory() + '\'' +
                ", originalPrice=" + product.getOriginalPrice() +
                ", stockQty=" + product.getStockQty() +
                ", version=" + product.getVersion() +
                ", status=" + product.getStatus() +
                '}';
    }

    private static String describeFlashSaleEvent(FlashSaleEvent event) {
        return "FlashSaleEvent{id='" + event.getId() + '\'' +
                ", createdAt=" + event.getCreatedAt() +
                ", updatedAt=" + event.getUpdatedAt() +
                ", eventName='" + event.getEventName() + '\'' +
                ", startTime=" + event.getStartTime() +
                ", endTime=" + event.getEndTime() +
                ", status=" + event.getStatus() +
                '}';
    }

    private static String describeFlashSaleItem(FlashSaleItem item) {
        return "FlashSaleItem{id='" + item.getId() + '\'' +
                ", createdAt=" + item.getCreatedAt() +
                ", updatedAt=" + item.getUpdatedAt() +
                ", eventId='" + item.getEventId() + '\'' +
                ", productId='" + item.getProductId() + '\'' +
                ", flashPrice=" + item.getFlashPrice() +
                ", limitedQty=" + item.getLimitedQty() +
                ", soldQty=" + item.getSoldQty() +
                ", discountPercent=" + item.getDiscountPercent() +
                ", version=" + item.getVersion() +
                ", status=" + item.getStatus() +
                '}';
    }

    private static void assertBaseFields(model.BaseEntity.BaseEntity entity, String expectedId) {
        assertEquals(expectedId, entity.getId());
        assertEquals(CREATED_AT, entity.getCreatedAt());
        assertEquals(UPDATED_AT, entity.getUpdatedAt());
    }

    private static void assertEquals(Object expected, Object actual) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError("Expected <" + expected + "> but was <" + actual + ">");
        }
    }

    private static void assertEquals(double expected, double actual) {
        if (Math.abs(expected - actual) > 0.000001) {
            throw new AssertionError("Expected <" + expected + "> but was <" + actual + ">");
        }
    }

    private static void assertEquals(long expected, long actual) {
        if (expected != actual) {
            throw new AssertionError("Expected <" + expected + "> but was <" + actual + ">");
        }
    }

    private static void assertEquals(boolean expected, boolean actual) {
        if (expected != actual) {
            throw new AssertionError("Expected <" + expected + "> but was <" + actual + ">");
        }
    }
}
