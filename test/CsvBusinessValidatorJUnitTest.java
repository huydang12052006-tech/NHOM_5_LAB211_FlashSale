package test;
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
        for (FlashItem item : dataset.flashItems.values()) {
            assertTrue(dataset.events.containsKey(item.eventId),
                    "FK_FLASHITEM_EVENT -> " + item.id);
            assertTrue(dataset.products.containsKey(item.productId),
                    "FK_FLASHITEM_PRODUCT -> " + item.id);
        }

        for (Product product : dataset.products.values()) {
            User seller = dataset.users.get(product.sellerId);
            assertTrue(seller != null && "SELLER".equals(seller.role),
                    "FK_PRODUCT_SELLER -> " + product.id + " sellerId=" + product.sellerId);
        }

        for (Order order : dataset.orders.values()) {
            assertTrue(dataset.customers.containsKey(order.customerId),
                    "FK_ORDER_CUSTOMER -> " + order.id);
            if (order.eventId != null && !order.eventId.isEmpty()) {
                assertTrue(dataset.events.containsKey(order.eventId),
                        "FK_ORDER_EVENT -> " + order.id);
            }
        }

        for (Customer customer : dataset.customers.values()) {
            User user = dataset.users.get(customer.userId);
            assertTrue(user != null,
                    "FK_CUSTOMER_USER -> " + customer.id);
            if (user != null) {
                assertTrue("CUSTOMER".equals(user.role),
                        "CUSTOMER_USER_ROLE -> " + customer.id
                                + " userId=" + customer.userId
                                + " role=" + user.role);
            }
        }

        for (OrderDetail detail : dataset.orderDetails) {
            assertTrue(dataset.orders.containsKey(detail.orderId),
                    "FK_DETAIL_ORDER -> " + detail.id);
            boolean flashDetail = detail.flashItemId != null && !detail.flashItemId.isEmpty();
            boolean regularDetail = detail.productId != null && !detail.productId.isEmpty();
            assertTrue(flashDetail || regularDetail,
                    "DETAIL_PRODUCT_REFERENCE -> " + detail.id);
            if (flashDetail) {
                assertTrue(dataset.flashItems.containsKey(detail.flashItemId),
                        "FK_DETAIL_FLASHITEM -> " + detail.id);
            }
            if (regularDetail) {
                assertTrue(dataset.products.containsKey(detail.productId),
                        "FK_DETAIL_PRODUCT -> " + detail.id);
            }
        }

        for (Payment payment : dataset.payments) {
            assertTrue(dataset.orders.containsKey(payment.orderId),
                    "FK_PAYMENT_ORDER -> " + payment.id);
            assertTrue(dataset.customers.containsKey(payment.customerId),
                    "FK_PAYMENT_CUSTOMER -> " + payment.id);
        }

        for (Transaction transaction : dataset.transactions) {
            assertTrue(dataset.orders.containsKey(transaction.orderId),
                    "FK_TRANSACTION_ORDER -> " + transaction.id);
        }

        for (CartItem cartItem : dataset.cartItems) {
            assertTrue(dataset.customers.containsKey(cartItem.customerId),
                    "FK_CART_CUSTOMER -> " + cartItem.id);
            assertTrue(cartItem.productId != null && !cartItem.productId.isEmpty()
                            && dataset.products.containsKey(cartItem.productId),
                    "FK_CART_PRODUCT -> " + cartItem.id);
            assertTrue(cartItem.quantity > 0, "CART_QUANTITY -> " + cartItem.id);
            if (cartItem.flashItemId != null && !cartItem.flashItemId.isEmpty()) {
                FlashItem flashItem = dataset.flashItems.get(cartItem.flashItemId);
                assertTrue(flashItem != null, "FK_CART_FLASHITEM -> " + cartItem.id);
                if (flashItem != null) {
                    assertTrue(flashItem.productId.equals(cartItem.productId),
                            "CART_FLASH_PRODUCT_MATCH -> " + cartItem.id);
                }
            }
        }
    }

    @Test
    void flashSaleRulesAreValid() {
        for (FlashItem item : dataset.flashItems.values()) {
            Product product = dataset.products.get(item.productId);
            if (product == null) {
                continue;
            }

            assertTrue(item.flashPrice < product.originalPrice,
                    "FLASH_PRICE_LT_ORIGINAL -> " + item.id);
            assertTrue(item.soldQty <= item.limitedQty,
                    "SOLD_QTY_LIMIT -> " + item.id);
        }
    }

    @Test
    void orderTotalsMatchDetails() {
        Map<String, Long> totals = new HashMap<>();

        for (OrderDetail detail : dataset.orderDetails) {
            totals.merge(detail.orderId, detail.subTotal, Long::sum);
        }

        for (Order order : dataset.orders.values()) {
            long expected = totals.getOrDefault(order.id, 0L);
            assertTrue(expected == order.totalAmount,
                    "ORDER_TOTAL -> " + order.id
                            + " expected=" + expected
                            + " actual=" + order.totalAmount);
        }
    }

    @Test
    void customerSpentMatchesSuccessfulOrders() {
        Map<String, Long> spent = new HashMap<>();

        for (Order order : dataset.orders.values()) {
            if ("SUCCESS".equals(order.status)) {
                spent.merge(order.customerId, order.totalAmount, Long::sum);
            }
        }

        for (Customer customer : dataset.customers.values()) {
            long expected = spent.getOrDefault(customer.id, 0L);
            assertTrue(expected == customer.totalSpent,
                    "CUSTOMER_SPENT -> " + customer.id
                            + " expected=" + expected
                            + " actual=" + customer.totalSpent);
        }
    }

    @Test
    void soldQuantitiesMatchSuccessfulOrderDetails() {
        Map<String, Integer> sold = new HashMap<>();

        for (OrderDetail detail : dataset.orderDetails) {
            Order order = dataset.orders.get(detail.orderId);

            if (order != null && "SUCCESS".equals(order.status)) {
                if (detail.flashItemId == null || detail.flashItemId.isEmpty()) {
                    continue;
                }
                sold.merge(detail.flashItemId, detail.quantity, Integer::sum);
            }
        }

        for (FlashItem item : dataset.flashItems.values()) {
            int expected = sold.getOrDefault(item.id, 0);
            assertTrue(expected == item.soldQty,
                    "SOLD_QTY -> " + item.id
                            + " expected=" + expected
                            + " actual=" + item.soldQty);
        }
    }

    @Test
    void purchaseLimitIsNotExceeded() {
        Map<String, Integer> counter = new HashMap<>();

        for (OrderDetail detail : dataset.orderDetails) {
            Order order = dataset.orders.get(detail.orderId);
            FlashItem item = dataset.flashItems.get(detail.flashItemId);

            if (order == null || item == null || order.eventId == null || order.eventId.isEmpty()) {
                continue;
            }

            String key = order.customerId + "_" + order.eventId + "_"
                    + item.productId;
            counter.merge(key, detail.quantity, Integer::sum);
        }

        for (Map.Entry<String, Integer> entry : counter.entrySet()) {
            assertTrue(entry.getValue() <= 2,
                    "MAX_2_RULE -> " + entry.getKey()
                            + " qty=" + entry.getValue());
        }
    }

    @Test
    void everyOrderHasDetail() {
        Set<String> orderIdsWithDetail = new HashSet<>();

        for (OrderDetail detail : dataset.orderDetails) {
            orderIdsWithDetail.add(detail.orderId);
        }

        for (Order order : dataset.orders.values()) {
            assertTrue(orderIdsWithDetail.contains(order.id),
                    "EMPTY_ORDER -> " + order.id);
        }
    }

    @Test
    void paymentAmountsMatchOrders() {
        for (Payment payment : dataset.payments) {
            Order order = dataset.orders.get(payment.orderId);
            if (order == null) {
                continue;
            }

            assertTrue(payment.amount == order.totalAmount,
                    "PAYMENT_AMOUNT -> " + payment.id);
        }
    }

    @Test
    void timestampsAreInValidOrder() {
        Map<String, Payment> paymentMap = new HashMap<>();

        for (Payment payment : dataset.payments) {
            paymentMap.put(payment.orderId, payment);

            Order order = dataset.orders.get(payment.orderId);
            if (order != null) {
                assertTrue(!payment.createdAt.isBefore(order.createdAt),
                        "PAYMENT_TIME -> " + payment.id);
            }
        }

        for (Transaction transaction : dataset.transactions) {
            Payment payment = paymentMap.get(transaction.orderId);
            if (payment != null) {
                assertTrue(!transaction.createdAt.isBefore(payment.createdAt),
                        "TRANSACTION_TIME -> " + transaction.id);
            }
        }
    }

    private static List<String[]> readCsv(String file) throws Exception {
        List<String[]> rows = new ArrayList<>();
        List<String> lines = Files.readAllLines(Paths.get("data", file));

        int firstDataRow = !lines.isEmpty() && lines.get(0).startsWith("id,") ? 1 : 0;
        for (int i = firstDataRow; i < lines.size(); i++) {
            rows.add(lines.get(i).split(","));
        }

        return rows;
    }

    private static long parseAmount(String value) {
        return Math.round(Double.parseDouble(value));
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
        private final List<CartItem> cartItems = new ArrayList<>();

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
            dataset.loadCartItems();
            return dataset;
        }

        private void loadProducts() throws Exception {
            for (String[] s : readCsv("products.csv")) {
                Product product = new Product();
                product.id = s[0];
                product.originalPrice = parseAmount(s[5]);
                product.sellerId = s[9];
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
                item.flashPrice = parseAmount(s[5]);
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
                customer.totalSpent = parseAmount(s[8]);
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
                order.totalAmount = parseAmount(s[5]);
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
                if (s.length >= 9) {
                    detail.productId = s[5];
                    detail.quantity = Integer.parseInt(s[6]);
                    detail.subTotal = parseAmount(s[8]);
                } else {
                    detail.quantity = Integer.parseInt(s[5]);
                    detail.subTotal = parseAmount(s[7]);
                }
                orderDetails.add(detail);
            }
        }

        private void loadPayments() throws Exception {
            Path path = Paths.get("data", "paytransactions.csv");
            if (!Files.exists(path)) {
                return;
            }

            for (String[] s : readCsv("paytransactions.csv")) {
                Payment payment = new Payment();
                payment.id = s[0];
                payment.createdAt = LocalDateTime.parse(s[1]);
                payment.orderId = s[3];
                payment.customerId = s[4];
                payment.amount = parseAmount(s[6]);
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
                if (s.length >= 12 && !isDateTime(s[1])) {
                    transaction.createdAt = LocalDateTime.parse(s[10]);
                    transaction.orderId = "";
                } else {
                    transaction.createdAt = LocalDateTime.parse(s[1]);
                    transaction.orderId = s[3];
                }
                transactions.add(transaction);
            }
        }

        private boolean isDateTime(String value) {
            try {
                LocalDateTime.parse(value);
                return true;
            } catch (RuntimeException e) {
                return false;
            }
        }

        private void loadCartItems() throws Exception {
            Path path = Paths.get("data", "cart_items.csv");
            if (!Files.exists(path)) {
                return;
            }
            for (String[] s : readCsv("cart_items.csv")) {
                CartItem item = new CartItem();
                item.id = s[0];
                item.customerId = s[3];
                item.flashItemId = s[4];
                item.productId = s[5];
                item.quantity = Integer.parseInt(s[6]);
                cartItems.add(item);
            }
        }
    }

    private static class Product {
        private String id;
        private long originalPrice;
        private String sellerId;
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
        private String productId;
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

    private static class CartItem {
        private String id;
        private String customerId;
        private String flashItemId;
        private String productId;
        private int quantity;
    }
}
