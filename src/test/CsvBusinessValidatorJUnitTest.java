import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CsvBusinessValidatorJUnitTest {

    private Dataset dataset;

    @BeforeEach
    void loadDataset() throws Exception {
        dataset = Dataset.load();
    }

    @Test
    void foreignKeysAreValid() {
        List<String> errors = new ArrayList<>();

        for (FlashItem item : dataset.flashItems.values()) {
            check(dataset.events.containsKey(item.eventId), errors,
                    "FK_FLASHITEM_EVENT -> " + item.id);
            check(dataset.products.containsKey(item.productId), errors,
                    "FK_FLASHITEM_PRODUCT -> " + item.id);
        }

        for (Order order : dataset.orders.values()) {
            check(dataset.customers.containsKey(order.customerId), errors,
                    "FK_ORDER_CUSTOMER -> " + order.id);
            check(dataset.events.containsKey(order.eventId), errors,
                    "FK_ORDER_EVENT -> " + order.id);
        }

        for (Customer customer : dataset.customers.values()) {
            User user = dataset.users.get(customer.userId);
            check(user != null, errors,
                    "FK_CUSTOMER_USER -> " + customer.id);
            if (user != null) {
                check("CUSTOMER".equals(user.role), errors,
                        "CUSTOMER_USER_ROLE -> " + customer.id
                                + " userId=" + customer.userId
                                + " role=" + user.role);
            }
        }

        for (OrderDetail detail : dataset.orderDetails) {
            check(dataset.orders.containsKey(detail.orderId), errors,
                    "FK_DETAIL_ORDER -> " + detail.id);
            check(dataset.flashItems.containsKey(detail.flashItemId), errors,
                    "FK_DETAIL_FLASHITEM -> " + detail.id);
        }

        for (Payment payment : dataset.payments) {
            check(dataset.orders.containsKey(payment.orderId), errors,
                    "FK_PAYMENT_ORDER -> " + payment.id);
            check(dataset.customers.containsKey(payment.customerId), errors,
                    "FK_PAYMENT_CUSTOMER -> " + payment.id);
        }

        for (Transaction transaction : dataset.transactions) {
            check(dataset.orders.containsKey(transaction.orderId), errors,
                    "FK_TRANSACTION_ORDER -> " + transaction.id);
        }

        assertNoErrors(errors);
    }

    @Test
    void flashSaleRulesAreValid() {
        List<String> errors = new ArrayList<>();

        for (FlashItem item : dataset.flashItems.values()) {
            Product product = dataset.products.get(item.productId);
            if (product == null) {
                continue;
            }

            check(item.flashPrice < product.originalPrice, errors,
                    "FLASH_PRICE_LT_ORIGINAL -> " + item.id);
            check(item.soldQty <= item.limitedQty, errors,
                    "SOLD_QTY_LIMIT -> " + item.id);
        }

        assertNoErrors(errors);
    }

    @Test
    void orderTotalsMatchDetails() {
        Map<String, Long> totals = new HashMap<>();
        List<String> errors = new ArrayList<>();

        for (OrderDetail detail : dataset.orderDetails) {
            totals.merge(detail.orderId, detail.subTotal, Long::sum);
        }

        for (Order order : dataset.orders.values()) {
            long expected = totals.getOrDefault(order.id, 0L);
            check(expected == order.totalAmount, errors,
                    "ORDER_TOTAL -> " + order.id
                            + " expected=" + expected
                            + " actual=" + order.totalAmount);
        }

        assertNoErrors(errors);
    }

    @Test
    void customerSpentMatchesSuccessfulOrders() {
        Map<String, Long> spent = new HashMap<>();
        List<String> errors = new ArrayList<>();

        for (Order order : dataset.orders.values()) {
            if ("SUCCESS".equals(order.status)) {
                spent.merge(order.customerId, order.totalAmount, Long::sum);
            }
        }

        for (Customer customer : dataset.customers.values()) {
            long expected = spent.getOrDefault(customer.id, 0L);
            check(expected == customer.totalSpent, errors,
                    "CUSTOMER_SPENT -> " + customer.id
                            + " expected=" + expected
                            + " actual=" + customer.totalSpent);
        }

        assertNoErrors(errors);
    }

    @Test
    void soldQuantitiesMatchSuccessfulOrderDetails() {
        Map<String, Integer> sold = new HashMap<>();
        List<String> errors = new ArrayList<>();

        for (OrderDetail detail : dataset.orderDetails) {
            Order order = dataset.orders.get(detail.orderId);

            if (order != null && "SUCCESS".equals(order.status)) {
                sold.merge(detail.flashItemId, detail.quantity, Integer::sum);
            }
        }

        for (FlashItem item : dataset.flashItems.values()) {
            int expected = sold.getOrDefault(item.id, 0);
            check(expected == item.soldQty, errors,
                    "SOLD_QTY -> " + item.id
                            + " expected=" + expected
                            + " actual=" + item.soldQty);
        }

        assertNoErrors(errors);
    }

    @Test
    void purchaseLimitIsNotExceeded() {
        Map<String, Integer> counter = new HashMap<>();
        List<String> errors = new ArrayList<>();

        for (OrderDetail detail : dataset.orderDetails) {
            Order order = dataset.orders.get(detail.orderId);
            FlashItem item = dataset.flashItems.get(detail.flashItemId);

            if (order == null || item == null) {
                continue;
            }

            String key = order.customerId + "_" + order.eventId + "_"
                    + item.productId;
            counter.merge(key, detail.quantity, Integer::sum);
        }

        for (Map.Entry<String, Integer> entry : counter.entrySet()) {
            check(entry.getValue() <= 2, errors,
                    "MAX_2_RULE -> " + entry.getKey()
                            + " qty=" + entry.getValue());
        }

        assertNoErrors(errors);
    }

    @Test
    void everyOrderHasDetail() {
        Set<String> orderIdsWithDetail = new HashSet<>();
        List<String> errors = new ArrayList<>();

        for (OrderDetail detail : dataset.orderDetails) {
            orderIdsWithDetail.add(detail.orderId);
        }

        for (Order order : dataset.orders.values()) {
            check(orderIdsWithDetail.contains(order.id), errors,
                    "EMPTY_ORDER -> " + order.id);
        }

        assertNoErrors(errors);
    }

    @Test
    void paymentAmountsMatchOrders() {
        List<String> errors = new ArrayList<>();

        for (Payment payment : dataset.payments) {
            Order order = dataset.orders.get(payment.orderId);
            if (order == null) {
                continue;
            }

            check(payment.amount == order.totalAmount, errors,
                    "PAYMENT_AMOUNT -> " + payment.id);
        }

        assertNoErrors(errors);
    }

    @Test
    void timestampsAreInValidOrder() {
        Map<String, Payment> paymentMap = new HashMap<>();
        List<String> errors = new ArrayList<>();

        for (Payment payment : dataset.payments) {
            paymentMap.put(payment.orderId, payment);

            Order order = dataset.orders.get(payment.orderId);
            if (order != null) {
                check(!payment.createdAt.isBefore(order.createdAt), errors,
                        "PAYMENT_TIME -> " + payment.id);
            }
        }

        for (Transaction transaction : dataset.transactions) {
            Payment payment = paymentMap.get(transaction.orderId);
            if (payment != null) {
                check(!transaction.createdAt.isBefore(payment.createdAt), errors,
                        "TRANSACTION_TIME -> " + transaction.id);
            }
        }

        assertNoErrors(errors);
    }

    private static void check(boolean ok, List<String> errors, String message) {
        if (!ok) {
            errors.add(message);
        }
    }

    private static void assertNoErrors(List<String> errors) {
        assertTrue(errors.isEmpty(), String.join(System.lineSeparator(), errors));
    }

    private static List<String[]> readCsv(String file) throws Exception {
        List<String[]> rows = new ArrayList<>();
        List<String> lines = Files.readAllLines(Paths.get("data", file));

        for (int i = 1; i < lines.size(); i++) {
            rows.add(lines.get(i).split(","));
        }

        return rows;
    }

    private static class Dataset {
        private final Map<String, Product> products = new HashMap<>();
        private final Map<String, Event> events = new HashMap<>();
        private final Map<String, FlashItem> flashItems = new HashMap<>();
        private final Map<String, User> users = new HashMap<>();
        private final Map<String, Customer> customers = new HashMap<>();
        private final Map<String, Order> orders = new HashMap<>();
        private final List<OrderDetail> orderDetails = new ArrayList<>();
        private final List<Payment> payments = new ArrayList<>();
        private final List<Transaction> transactions = new ArrayList<>();

        private static Dataset load() throws Exception {
            Dataset dataset = new Dataset();
            dataset.loadProducts();
            dataset.loadEvents();
            dataset.loadFlashItems();
            dataset.loadUsers();
            dataset.loadCustomers();
            dataset.loadOrders();
            dataset.loadOrderDetails();
            dataset.loadPayments();
            dataset.loadTransactions();
            return dataset;
        }

        private void loadProducts() throws Exception {
            for (String[] s : readCsv("products.csv")) {
                Product product = new Product();
                product.id = s[0];
                product.originalPrice = Long.parseLong(s[5]);
                products.put(product.id, product);
            }
        }

        private void loadEvents() throws Exception {
            for (String[] s : readCsv("flash_events.csv")) {
                Event event = new Event();
                event.id = s[0];
                events.put(event.id, event);
            }
        }

        private void loadFlashItems() throws Exception {
            for (String[] s : readCsv("flash_items.csv")) {
                FlashItem item = new FlashItem();
                item.id = s[0];
                item.eventId = s[3];
                item.productId = s[4];
                item.flashPrice = Long.parseLong(s[5]);
                item.limitedQty = Integer.parseInt(s[6]);
                item.soldQty = Integer.parseInt(s[7]);
                flashItems.put(item.id, item);
            }
        }

        private void loadUsers() throws Exception {
            for (String[] s : readCsv("users.csv")) {
                User user = new User();
                user.id = s[0];
                user.role = s[5];
                users.put(user.id, user);
            }
        }

        private void loadCustomers() throws Exception {
            for (String[] s : readCsv("customers.csv")) {
                Customer customer = new Customer();
                customer.id = s[0];
                customer.userId = s[3];
                customer.totalSpent = Long.parseLong(s[8]);
                customers.put(customer.id, customer);
            }
        }

        private void loadOrders() throws Exception {
            for (String[] s : readCsv("orders.csv")) {
                Order order = new Order();
                order.id = s[0];
                order.createdAt = LocalDateTime.parse(s[1]);
                order.customerId = s[3];
                order.eventId = s[4];
                order.totalAmount = Long.parseLong(s[5]);
                order.status = s[6];
                orders.put(order.id, order);
            }
        }

        private void loadOrderDetails() throws Exception {
            for (String[] s : readCsv("order_details.csv")) {
                OrderDetail detail = new OrderDetail();
                detail.id = s[0];
                detail.orderId = s[3];
                detail.flashItemId = s[4];
                detail.quantity = Integer.parseInt(s[5]);
                detail.subTotal = Long.parseLong(s[7]);
                orderDetails.add(detail);
            }
        }

        private void loadPayments() throws Exception {
            Path path = Paths.get("data", "payments.csv");
            if (!Files.exists(path)) {
                return;
            }

            for (String[] s : readCsv("payments.csv")) {
                Payment payment = new Payment();
                payment.id = s[0];
                payment.createdAt = LocalDateTime.parse(s[1]);
                payment.orderId = s[3];
                payment.customerId = s[4];
                payment.amount = Long.parseLong(s[6]);
                payments.add(payment);
            }
        }

        private void loadTransactions() throws Exception {
            Path path = Paths.get("data", "transactions.csv");
            if (!Files.exists(path)) {
                return;
            }

            for (String[] s : readCsv("transactions.csv")) {
                Transaction transaction = new Transaction();
                transaction.id = s[0];
                transaction.createdAt = LocalDateTime.parse(s[1]);
                transaction.orderId = s[3];
                transactions.add(transaction);
            }
        }
    }

    private static class Product {
        private String id;
        private long originalPrice;
    }

    private static class Event {
        private String id;
    }

    private static class FlashItem {
        private String id;
        private String eventId;
        private String productId;
        private long flashPrice;
        private int limitedQty;
        private int soldQty;
    }

    private static class Customer {
        private String id;
        private String userId;
        private long totalSpent;
    }

    private static class User {
        private String id;
        private String role;
    }

    private static class Order {
        private String id;
        private LocalDateTime createdAt;
        private String customerId;
        private String eventId;
        private long totalAmount;
        private String status;
    }

    private static class OrderDetail {
        private String id;
        private String orderId;
        private String flashItemId;
        private int quantity;
        private long subTotal;
    }

    private static class Payment {
        private String id;
        private LocalDateTime createdAt;
        private String orderId;
        private String customerId;
        private long amount;
    }

    private static class Transaction {
        private String id;
        private LocalDateTime createdAt;
        private String orderId;
    }
}
