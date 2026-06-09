
import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;

public class CsvBusinessValidator {

    private static final String DATA_DIR = "data/";

    private static int passed = 0;
    private static int failed = 0;

    private static final Map<String, Product> products = new HashMap<>();
    private static final Map<String, Event> events = new HashMap<>();
    private static final Map<String, FlashItem> flashItems = new HashMap<>();
    private static final Map<String, Customer> customers = new HashMap<>();
    private static final Map<String, Order> orders = new HashMap<>();
    private static final List<OrderDetail> orderDetails = new ArrayList<>();
    private static final List<Payment> payments = new ArrayList<>();
    private static final List<Transaction> transactions = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        loadProducts();
        loadEvents();
        loadFlashItems();
        loadCustomers();
        loadOrders();
        loadOrderDetails();
        loadPayments();
        loadTransactions();

        validateForeignKeys();
        validateFlashSaleRules();
        validateOrderTotals();
        validateCustomerSpent();
        validateSoldQty();
        validatePurchaseLimit();
        validateOrderHasDetail();
        validatePaymentAmounts();
        validateTimestamps();

        System.out.println("\n====================================");
        System.out.println("CSV DATASET VALIDATION");
        System.out.println("====================================");
        System.out.println("PASSED = " + passed);
        System.out.println("FAILED = " + failed);
        System.out.println(failed == 0 ? "DATASET CONSISTENT" : "DATASET HAS ERRORS");
    }

    private static void validateForeignKeys() {
        for (FlashItem f : flashItems.values()) {
            check(events.containsKey(f.eventId), "FK_FLASHITEM_EVENT", f.id);
            check(products.containsKey(f.productId), "FK_FLASHITEM_PRODUCT", f.id);
        }

        for (Order o : orders.values()) {
            check(customers.containsKey(o.customerId), "FK_ORDER_CUSTOMER", o.id);
            check(events.containsKey(o.eventId), "FK_ORDER_EVENT", o.id);
        }

        for (OrderDetail d : orderDetails) {
            check(orders.containsKey(d.orderId), "FK_DETAIL_ORDER", d.id);
            check(flashItems.containsKey(d.flashItemId), "FK_DETAIL_FLASHITEM", d.id);
        }

        for (Payment p : payments) {
            check(orders.containsKey(p.orderId), "FK_PAYMENT_ORDER", p.id);
            check(customers.containsKey(p.customerId), "FK_PAYMENT_CUSTOMER", p.id);
        }

        for (Transaction t : transactions) {
            check(orders.containsKey(t.orderId), "FK_TRANSACTION_ORDER", t.id);
        }
    }

    private static void validateFlashSaleRules() {
        for (FlashItem item : flashItems.values()) {
            Product p = products.get(item.productId);
            if (p == null) continue;

            check(item.flashPrice < p.originalPrice,
                    "FLASH_PRICE_LT_ORIGINAL", item.id);

            check(item.soldQty <= item.limitedQty,
                    "SOLD_QTY_LIMIT", item.id);
        }
    }

    private static void validateOrderTotals() {
        Map<String, Long> totals = new HashMap<>();

        for (OrderDetail d : orderDetails) {
            totals.merge(d.orderId, d.subTotal, Long::sum);
        }

        for (Order o : orders.values()) {
            long expected = totals.getOrDefault(o.id, 0L);

            check(expected == o.totalAmount,
                    "ORDER_TOTAL",
                    o.id + " expected=" + expected + " actual=" + o.totalAmount);
        }
    }

    private static void validateCustomerSpent() {
        Map<String, Long> spent = new HashMap<>();

        for (Order o : orders.values()) {
            if ("SUCCESS".equals(o.status)) {
                spent.merge(o.customerId, o.totalAmount, Long::sum);
            }
        }

        for (Customer c : customers.values()) {
            long expected = spent.getOrDefault(c.id, 0L);

            check(expected == c.totalSpent,
                    "CUSTOMER_SPENT",
                    c.id + " expected=" + expected + " actual=" + c.totalSpent);
        }
    }

    private static void validateSoldQty() {
        Map<String, Integer> sold = new HashMap<>();

        for (OrderDetail d : orderDetails) {
            Order o = orders.get(d.orderId);

            if (o != null && "SUCCESS".equals(o.status)) {
                sold.merge(d.flashItemId, d.quantity, Integer::sum);
            }
        }

        for (FlashItem item : flashItems.values()) {
            int expected = sold.getOrDefault(item.id, 0);

            check(expected == item.soldQty,
                    "SOLD_QTY",
                    item.id + " expected=" + expected + " actual=" + item.soldQty);
        }
    }

    private static void validatePurchaseLimit() {
        Map<String, Integer> counter = new HashMap<>();

        for (OrderDetail d : orderDetails) {
            Order o = orders.get(d.orderId);
            FlashItem f = flashItems.get(d.flashItemId);

            if (o == null || f == null) continue;

            String key = o.customerId + "_" + o.eventId + "_" + f.productId;

            counter.merge(key, d.quantity, Integer::sum);
        }

        for (Map.Entry<String,Integer> e : counter.entrySet()) {
            check(e.getValue() <= 2,
                    "MAX_2_RULE",
                    e.getKey() + " qty=" + e.getValue());
        }
    }

    private static void validateOrderHasDetail() {
        Set<String> ids = new HashSet<>();

        for (OrderDetail d : orderDetails) {
            ids.add(d.orderId);
        }

        for (Order o : orders.values()) {
            check(ids.contains(o.id), "EMPTY_ORDER", o.id);
        }
    }

    private static void validatePaymentAmounts() {
        for (Payment p : payments) {
            Order o = orders.get(p.orderId);
            if (o == null) continue;

            check(p.amount == o.totalAmount,
                    "PAYMENT_AMOUNT",
                    p.id);
        }
    }

    private static void validateTimestamps() {
        Map<String, Payment> paymentMap = new HashMap<>();

        for (Payment p : payments) {
            paymentMap.put(p.orderId, p);

            Order o = orders.get(p.orderId);

            if (o != null) {
                check(!p.createdAt.isBefore(o.createdAt),
                        "PAYMENT_TIME",
                        p.id);
            }
        }

        for (Transaction t : transactions) {
            Payment p = paymentMap.get(t.orderId);

            if (p != null) {
                check(!t.createdAt.isBefore(p.createdAt),
                        "TRANSACTION_TIME",
                        t.id);
            }
        }
    }

    private static void check(boolean ok, String rule, String msg) {
        if (ok) {
            passed++;
        } else {
            failed++;
            System.out.println("[FAIL] " + rule + " -> " + msg);
        }
    }

    private static List<String[]> readCsv(String file) throws Exception {
        List<String[]> rows = new ArrayList<>();
        List<String> lines = Files.readAllLines(Paths.get(DATA_DIR + file));

        for (int i = 1; i < lines.size(); i++) {
            rows.add(lines.get(i).split(","));
        }

        return rows;
    }

    private static void loadProducts() throws Exception {
        for (String[] s : readCsv("products.csv")) {
            Product p = new Product();
            p.id = s[0];
            p.originalPrice = Long.parseLong(s[5]);
            products.put(p.id, p);
        }
    }

    private static void loadEvents() throws Exception {
        for (String[] s : readCsv("flash_events.csv")) {
            Event e = new Event();
            e.id = s[0];
            events.put(e.id, e);
        }
    }

    private static void loadFlashItems() throws Exception {
        for (String[] s : readCsv("flash_items.csv")) {
            FlashItem f = new FlashItem();
            f.id = s[0];
            f.eventId = s[3];
            f.productId = s[4];
            f.flashPrice = Long.parseLong(s[5]);
            f.limitedQty = Integer.parseInt(s[6]);
            f.soldQty = Integer.parseInt(s[7]);
            flashItems.put(f.id, f);
        }
    }

    private static void loadCustomers() throws Exception {
        for (String[] s : readCsv("customers.csv")) {
            Customer c = new Customer();
            c.id = s[0];
            c.totalSpent = Long.parseLong(s[7]);
            customers.put(c.id, c);
        }
    }

    private static void loadOrders() throws Exception {
        for (String[] s : readCsv("orders.csv")) {
            Order o = new Order();
            o.id = s[0];
            o.createdAt = LocalDateTime.parse(s[1]);
            o.customerId = s[3];
            o.eventId = s[4];
            o.totalAmount = Long.parseLong(s[5]);
            o.status = s[6];
            orders.put(o.id, o);
        }
    }

    private static void loadOrderDetails() throws Exception {
        for (String[] s : readCsv("order_details.csv")) {
            OrderDetail d = new OrderDetail();
            d.id = s[0];
            d.orderId = s[3];
            d.flashItemId = s[4];
            d.quantity = Integer.parseInt(s[5]);
            d.subTotal = Long.parseLong(s[7]);
            orderDetails.add(d);
        }
    }

    private static void loadPayments() throws Exception {
        Path pth = Paths.get(DATA_DIR + "payments.csv");
        if (!Files.exists(pth)) return;

        for (String[] s : readCsv("payments.csv")) {
            Payment p = new Payment();
            p.id = s[0];
            p.createdAt = LocalDateTime.parse(s[1]);
            p.orderId = s[3];
            p.customerId = s[4];
            p.amount = Long.parseLong(s[6]);
            payments.add(p);
        }
    }

    private static void loadTransactions() throws Exception {
        Path pth = Paths.get(DATA_DIR + "transactions.csv");
        if (!Files.exists(pth)) return;

        for (String[] s : readCsv("transactions.csv")) {
            Transaction t = new Transaction();
            t.id = s[0];
            t.createdAt = LocalDateTime.parse(s[1]);
            t.orderId = s[3];
            transactions.add(t);
        }
    }

    static class Product {
        String id;
        long originalPrice;
    }

    static class Event {
        String id;
    }

    static class FlashItem {
        String id;
        String eventId;
        String productId;
        long flashPrice;
        int limitedQty;
        int soldQty;
    }

    static class Customer {
        String id;
        long totalSpent;
    }

    static class Order {
        String id;
        LocalDateTime createdAt;
        String customerId;
        String eventId;
        long totalAmount;
        String status;
    }

    static class OrderDetail {
        String id;
        String orderId;
        String flashItemId;
        int quantity;
        long subTotal;
    }

    static class Payment {
        String id;
        LocalDateTime createdAt;
        String orderId;
        String customerId;
        long amount;
    }

    static class Transaction {
        String id;
        LocalDateTime createdAt;
        String orderId;
    }
}
