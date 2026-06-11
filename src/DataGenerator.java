/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */

import java.util.Random;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataGenerator {

        private static final Random random = new Random();

        private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

        private static final String[] FIRST_NAMES = {
                        "Nguyen", "Tran", "Le", "Pham", "Hoang",
                        "Vo", "Dang", "Bui", "Do", "Huynh"
        };

        private static final String[] MIDDLE_NAMES = {
                        "Van", "Thi", "Minh", "Ngoc", "Thanh",
                        "Duc", "Quoc", "Gia", "Bao", "Anh"
        };

        private static final String[] LAST_NAMES = {
                        "An", "Binh", "Cuong", "Dung", "Giang",
                        "Hanh", "Khanh", "Linh", "Nam", "Phong",
                        "Quan", "Son", "Trang", "Vy", "Yen"
        };

        private static final String[] CATEGORIES = {
                        "Laptop",
                        "Smartphone",
                        "Tablet",
                        "Gaming",
                        "Fashion",
                        "HomeAppliance",
                        "Camera",
                        "Accessory",
                        "Audio",
                        "SmartWatch"
        };

        private static final String[][] PRODUCT_NAMES = {

                        { "MacBook Air M2", "Dell XPS 15", "Asus ROG Zephyrus",
                                        "Lenovo ThinkPad X1", "HP Pavilion Gaming" },

                        { "iPhone 15 Pro Max", "Samsung Galaxy S25",
                                        "Xiaomi 14 Ultra", "Oppo Find X7",
                                        "Google Pixel 9" },

                        { "iPad Air Gen 6", "Samsung Galaxy Tab S9",
                                        "Xiaomi Pad 7", "Huawei MatePad Pro",
                                        "Lenovo Tab Extreme" },

                        { "PlayStation 5", "Xbox Series X",
                                        "Nintendo Switch OLED", "MSI Gaming PC",
                                        "Razer Blade 16" },

                        { "Nike Air Force 1", "Adidas Ultraboost",
                                        "Uniqlo Hoodie", "Levis 501 Jeans",
                                        "Puma Running Shoes" },

                        { "Dyson Vacuum V15", "Philips Air Fryer",
                                        "Panasonic Refrigerator", "LG Washing Machine",
                                        "Sharp Microwave Oven" },

                        { "Canon EOS R6", "Sony A7 IV",
                                        "Fujifilm XT5", "Nikon Z8",
                                        "GoPro Hero 13" },

                        { "Logitech MX Master 3S", "Anker PowerBank 20000mAh",
                                        "UGreen USB-C Hub", "Baseus Charger 100W",
                                        "Apple Magic Mouse" },

                        { "Sony WH-1000XM6", "AirPods Pro 3",
                                        "JBL Charge 6", "Marshall Stanmore",
                                        "HyperX Cloud III" },

                        { "Apple Watch Ultra 3", "Samsung Galaxy Watch 8",
                                        "Garmin Forerunner 965", "Huawei Watch GT5",
                                        "Xiaomi Watch S4" }
        };

        private static final String[] ORDER_STATUS = {
                        "PENDING", "SUCCESS", "FAILED", "CANCELLED"
        };

        private static final String[] LOCKS = {
                        "NO_LOCK",
                        "SYNCHRONIZED",
                        "FILE_LOCK",
                        "OPTIMISTIC_LOCK"
        };

        private static final String[] USER_ROLES = {
                        "CUSTOMER", "SELLER", "ADMIN"
        };

        private static final String[] PAYMENT_METHODS = {
                        "CASH", "BANKING"
        };

        private static final String[] THREAD_NAMES = {
                        "Thread-1", "Thread-2", "Thread-3", "Thread-4",
                        "Thread-5", "Thread-6", "Thread-7", "Thread-8",
                        "pool-1-thread-1", "pool-1-thread-2", "pool-1-thread-3",
                        "pool-2-thread-1", "pool-2-thread-2", "pool-2-thread-3"
        };

        private static final String[] TX_MESSAGES_SUCCESS = {
                        "Order placed successfully",
                        "Transaction completed",
                        "Payment confirmed",
                        "Order processed successfully",
                        "Flash sale item purchased"
        };

        private static final String[] TX_MESSAGES_FAIL = {
                        "Insufficient stock",
                        "Optimistic lock conflict",
                        "File lock timeout",
                        "Concurrent modification detected",
                        "Item sold out",
                        "Transaction timeout"
        };

        // =========================================================
        // Shared data structures for cross-referencing
        // =========================================================

        // productId -> originalPrice
        private static final Map<String, Double> productPriceMap = new HashMap<>();

        // eventId -> list of flashItemIds belonging to that event
        private static final Map<String, List<String>> eventFlashItemsMap = new HashMap<>();

        // flashItemId -> flashPrice
        private static final Map<String, Double> flashItemPriceMap = new HashMap<>();

        // flashItemId -> productId
        private static final Map<String, String> flashItemProductMap = new HashMap<>();

        // flashItemId -> soldQty (accumulated from OrderDetails)
        private static final Map<String, Integer> flashItemSoldQtyMap = new HashMap<>();

        // flashItemId -> limitedQty
        private static final Map<String, Integer> flashItemLimitedQtyMap = new HashMap<>();

        // orderId -> list of (flashItemId, quantity, unitPrice, subTotal)
        private static final Map<String, List<double[]>> orderDetailsMap = new HashMap<>();

        // orderId -> list of detail string-data for flashItemIds
        private static final Map<String, List<String>> orderDetailFlashItemIds = new HashMap<>();

        // customerId -> totalSpent (sum of order totalAmounts for SUCCESS orders)
        private static final Map<String, Double> customerSpentMap = new HashMap<>();

        // userId -> customer fullName, used to keep usernames aligned with customers
        private static final Map<String, String> customerFullNameByUserId = new HashMap<>();

        // orderId -> customerId
        private static final Map<String, String> orderCustomerMap = new HashMap<>();

        // orderId -> eventId
        private static final Map<String, String> orderEventMap = new HashMap<>();

        // orderId -> totalAmount
        private static final Map<String, Double> orderTotalMap = new HashMap<>();

        // customerId + "_" + eventId + "_" + productId -> quantity purchased in that event (enforce max 2)
        private static final Map<String, Integer> customerEventProductQty = new HashMap<>();

        // orderId -> order createdAt (for timeline consistency)
        private static final Map<String, java.time.LocalDateTime> orderCreatedAtMap = new HashMap<>();

        // orderId -> payment createdAt (for transaction ordering)
        private static final Map<String, java.time.LocalDateTime> orderPaymentTimeMap = new HashMap<>();

        // orderId -> status
        private static final Map<String, String> orderStatusMap = new HashMap<>();

        // orderId -> lockMechanism
        private static final Map<String, String> orderLockMap = new HashMap<>();

        // Store event start/end times for reference
        private static final Map<String, LocalDateTime> eventStartMap = new HashMap<>();
        private static final Map<String, LocalDateTime> eventEndMap = new HashMap<>();

        // =========================================================

        private static final int NUM_PRODUCTS = 5000;
        private static final int NUM_EVENTS = 50;
        private static final int NUM_FLASH_ITEMS = 1000;
        private static final int NUM_CUSTOMERS = 2500;
        private static final int NUM_ORDERS = 3000;
        private static final int NUM_USERS = NUM_CUSTOMERS + 100;

        public static void main(String[] args) {

                new File("data").mkdirs();

                try {

                        // Phase 1: Generate independent entities
                        generateProducts(NUM_PRODUCTS);
                        generateFlashEvents(NUM_EVENTS);

                        // Phase 2: Generate FlashItems linked to real Products & Events
                        generateFlashItems(NUM_FLASH_ITEMS);

                        // Phase 3: Generate Customers (initially without totalSpent)
                        generateCustomersPhase1(NUM_CUSTOMERS);

                        // Phase 4: Generate Orders + OrderDetails together (consistent)
                        generateOrdersAndDetails(NUM_ORDERS);

                        // Phase 5: Update FlashItems CSV with correct soldQty
                        rewriteFlashItemsWithSoldQty();

                        // Phase 6: Rewrite Customers with correct userId, totalSpent and tier
                        rewriteCustomersWithSpent();

                        // Phase 7: Generate Users CSV using the linked customer names
                        generateUsers(NUM_USERS);

                        // Phase 8: Generate Payments CSV (one per SUCCESS order)
                        generatePayments();

                        // Phase 9: Generate OrderTransactions CSV
                        generateOrderTransactions();

                        System.out.println("Generate CSV SUCCESS!");

                } catch (Exception e) {
                        e.printStackTrace();
                }
        }

        // =========================================================
        // PRODUCTS
        // =========================================================

        private static void generateProducts(int count) throws IOException {

                BufferedWriter bw = new BufferedWriter(new FileWriter("data/products.csv"));

                bw.write("id,createdAt,updatedAt,name,category,originalPrice,stockQty,version,status");
                bw.newLine();

                for (int i = 1; i <= count; i++) {

                        int categoryIndex = random.nextInt(CATEGORIES.length);

                        String category = CATEGORIES[categoryIndex];

                        String productName = PRODUCT_NAMES[categoryIndex][random
                                        .nextInt(PRODUCT_NAMES[categoryIndex].length)];

                        String fullProductName = productName + " " +
                                        (2023 + random.nextInt(4)) +
                                        " Edition";

                        String id = "P" + String.format("%05d", i);

                        LocalDateTime createdAt = randomDateTime();
                        LocalDateTime updatedAt = randomUpdatedAt(createdAt);

                        double price = 500000 + random.nextInt(50000000);

                        int stockQty = 100 + random.nextInt(5000);

                        int version = random.nextInt(20);

                        // Product status: ACTIVE or DISABLED (not UPCOMING/ENDED)
                        String status = random.nextInt(10) < 8 ? "ACTIVE" : "DISABLED";

                        // Store price for FlashItem reference
                        productPriceMap.put(id, price);

                        bw.write(String.join(",",
                                        id,
                                        createdAt.format(formatter),
                                        updatedAt.format(formatter),
                                        fullProductName,
                                        category,
                                        String.valueOf((long) price),
                                        String.valueOf(stockQty),
                                        String.valueOf(version),
                                        status));

                        bw.newLine();
                }

                bw.close();
        }

        // =========================================================
        // FLASH EVENTS
        // =========================================================

        private static void generateFlashEvents(int count)
                        throws IOException {

                BufferedWriter bw = new BufferedWriter(new FileWriter("data/flash_events.csv"));

                bw.write("id,createdAt,updatedAt,eventName,startTime,endTime,status");
                bw.newLine();

                LocalDateTime now = LocalDateTime.now();

                for (int i = 1; i <= count; i++) {

                        String id = "E" + String.format("%03d", i);

                        LocalDateTime createdAt = randomDateTime();
                        LocalDateTime updatedAt = randomUpdatedAt(createdAt);

                        LocalDateTime start = now.minusDays(random.nextInt(30))
                                        .plusHours(random.nextInt(24));

                        LocalDateTime end = start.plusHours(2 + random.nextInt(5));

                        String eventName = "Mega Flash Sale " + (i);

                        // Derive status from start/end time relative to now
                        String status;
                        if (now.isBefore(start)) {
                                status = "UPCOMING";
                        } else if (now.isAfter(end)) {
                                status = "ENDED";
                        } else {
                                status = "ACTIVE";
                        }

                        eventStartMap.put(id, start);
                        eventEndMap.put(id, end);
                        eventFlashItemsMap.put(id, new ArrayList<>());

                        bw.write(String.join(",",
                                        id,
                                        createdAt.format(formatter),
                                        updatedAt.format(formatter),
                                        eventName,
                                        start.format(formatter),
                                        end.format(formatter),
                                        status));

                        bw.newLine();
                }

                bw.close();
        }

        // =========================================================
        // FLASH ITEMS (Phase 2 - linked to real Products)
        // =========================================================

        private static void generateFlashItems(int count)
                        throws IOException {

                // Ensure every event has at least one FlashItem first
                BufferedWriter bw = new BufferedWriter(new FileWriter("data/flash_items.csv"));

                bw.write("id,createdAt,updatedAt,eventId,productId,flashPrice,limitedQty,soldQty,discountPercent,version,status");
                bw.newLine();

                int idx = 1;
                // Phase A: create one item per event to guarantee coverage
                for (int e = 1; e <= NUM_EVENTS && idx <= count; e++, idx++) {
                        String id = "FI" + String.format("%05d", idx);

                        LocalDateTime createdAt = randomDateTime();
                        LocalDateTime updatedAt = randomUpdatedAt(createdAt);

                        String eventId = "E" + String.format("%03d", e);

                        // Pick a real product
                        String productId = "P" + String.format("%05d",
                                        1 + random.nextInt(NUM_PRODUCTS));

                        double originalPrice = productPriceMap.get(productId);

                        // Discount between 10% and 70%
                        int discountPct = 10 + random.nextInt(61);
                        double flashPrice = Math.round(originalPrice * (100 - discountPct) / 100.0);

                        int limitedQty = 10 + random.nextInt(200);

                        // soldQty starts at 0, will be rewritten in Phase 5
                        int soldQty = 0;

                        int version = 1; // deterministic initial version

                        String status;
                        LocalDateTime eventEnd = eventEndMap.get(eventId);
                        LocalDateTime eventStart = eventStartMap.get(eventId);
                        LocalDateTime now = LocalDateTime.now();
                        if (now.isBefore(eventStart)) {
                                status = "UPCOMING";
                        } else if (now.isAfter(eventEnd)) {
                                status = "ENDED";
                        } else {
                                status = "ACTIVE";
                        }

                        // Register flash item in maps
                        eventFlashItemsMap.get(eventId).add(id);
                        flashItemPriceMap.put(id, flashPrice);
                        flashItemProductMap.put(id, productId);
                        flashItemSoldQtyMap.put(id, 0);
                        flashItemLimitedQtyMap.put(id, limitedQty);

                        bw.write(String.join(",",
                                        id,
                                        createdAt.format(formatter),
                                        updatedAt.format(formatter),
                                        eventId,
                                        productId,
                                        String.valueOf((long) flashPrice),
                                        String.valueOf(limitedQty),
                                        String.valueOf(soldQty),
                                        String.valueOf(discountPct),
                                        String.valueOf(version),
                                        status));

                        bw.newLine();
                }

                // Phase B: create remaining items and distribute randomly
                for (; idx <= count; idx++) {

                        String id = "FI" + String.format("%05d", idx);

                        LocalDateTime createdAt = randomDateTime();
                        LocalDateTime updatedAt = randomUpdatedAt(createdAt);

                        String eventId = "E" + String.format("%03d",
                                        1 + random.nextInt(NUM_EVENTS));

                        // Pick a real product
                        String productId = "P" + String.format("%05d",
                                        1 + random.nextInt(NUM_PRODUCTS));

                        double originalPrice = productPriceMap.get(productId);

                        // Discount between 10% and 70%
                        int discountPct = 10 + random.nextInt(61);
                        double flashPrice = Math.round(originalPrice * (100 - discountPct) / 100.0);

                        int limitedQty = 10 + random.nextInt(200);

                        int soldQty = 0;

                        int version = 1;

                        String status;
                        LocalDateTime eventEnd = eventEndMap.get(eventId);
                        LocalDateTime eventStart = eventStartMap.get(eventId);
                        LocalDateTime now = LocalDateTime.now();
                        if (now.isBefore(eventStart)) {
                                status = "UPCOMING";
                        } else if (now.isAfter(eventEnd)) {
                                status = "ENDED";
                        } else {
                                status = "ACTIVE";
                        }

                        // Register flash item in maps
                        eventFlashItemsMap.get(eventId).add(id);
                        flashItemPriceMap.put(id, flashPrice);
                        flashItemProductMap.put(id, productId);
                        flashItemSoldQtyMap.put(id, 0);
                        flashItemLimitedQtyMap.put(id, limitedQty);

                        bw.write(String.join(",",
                                        id,
                                        createdAt.format(formatter),
                                        updatedAt.format(formatter),
                                        eventId,
                                        productId,
                                        String.valueOf((long) flashPrice),
                                        String.valueOf(limitedQty),
                                        String.valueOf(soldQty),
                                        String.valueOf(discountPct),
                                        String.valueOf(version),
                                        status));

                        bw.newLine();
                }

                bw.close();
        }

        // =========================================================
        // CUSTOMERS Phase 1 (generate with placeholder totalSpent)
        // =========================================================

        private static void generateCustomersPhase1(int count)
                        throws IOException {

                // Phase 1: just register customers, don't write final CSV yet
                for (int i = 1; i <= count; i++) {
                        String id = "C" + String.format("%05d", i);
                        customerSpentMap.put(id, 0.0);
                }
        }

        // =========================================================
        // ORDERS + ORDER DETAILS (generated together for consistency)
        // =========================================================

        private static void generateOrdersAndDetails(int orderCount)
                        throws IOException {

                BufferedWriter bwOrders = new BufferedWriter(new FileWriter("data/orders.csv"));
                BufferedWriter bwDetails = new BufferedWriter(new FileWriter("data/order_details.csv"));

                bwOrders.write("id,createdAt,updatedAt,customerId,eventId,totalAmount,status,lockMechanism");
                bwOrders.newLine();

                bwDetails.write("id,createdAt,updatedAt,orderId,flashItemId,quantity,unitPrice,subTotal");
                bwDetails.newLine();

                int detailCounter = 0;

                for (int i = 1; i <= orderCount; i++) {

                        String orderId = "O" + String.format("%06d", i);

                        LocalDateTime createdAt = randomDateTime();
                        LocalDateTime updatedAt = randomUpdatedAt(createdAt);

                        // record order createdAt for later timestamp consistency
                        orderCreatedAtMap.put(orderId, createdAt);

                        String customerId = "C" + String.format("%05d",
                                        1 + random.nextInt(NUM_CUSTOMERS));

                        // Pick an event that has flash items
                        String eventId = pickEventWithItems();

                        String status = ORDER_STATUS[random.nextInt(ORDER_STATUS.length)];

                        String lock = LOCKS[random.nextInt(LOCKS.length)];

                        // Generate 1-3 OrderDetails for this order
                        int numDetails = 1 + random.nextInt(3);

                        double totalAmount = 0;

                        List<String> eventItems = eventFlashItemsMap.get(eventId);

                        java.util.Set<String> usedFlashItems = new java.util.HashSet<>();

                        for (int d = 0; d < numDetails; d++) {

                                detailCounter++;
                                String detailId = "OD" + String.format("%06d", detailCounter);

                                // Try to find a flashItem that:
                                // - is not already used in this order
                                // - has stock remaining
                                // - respects the per-customer per-event per-product limit (max 2)
                                String flashItemId = null;
                                String productId = null;
                                int attempts = 0;
                                int maxAttempts = Math.max(10, eventItems.size() * 2);
                                while (attempts < maxAttempts) {
                                        attempts++;
                                        String candidate = eventItems.get(random.nextInt(eventItems.size()));
                                        if (usedFlashItems.contains(candidate)) {
                                                continue;
                                        }
                                        int currentSold = flashItemSoldQtyMap.getOrDefault(candidate, 0);
                                        int limitedQty = flashItemLimitedQtyMap.get(candidate);
                                        int stockRemaining = limitedQty - currentSold;
                                        if (stockRemaining <= 0) {
                                                continue; // sold out
                                        }
                                        String candidateProduct = flashItemProductMap.get(candidate);
                                        String key = customerId + "_" + eventId + "_" + candidateProduct;
                                        int currentCustomerQty = customerEventProductQty.getOrDefault(key, 0);
                                        if (currentCustomerQty >= 2) {
                                                continue; // customer already reached limit for this product in this event
                                        }
                                        // Accept candidate
                                        flashItemId = candidate;
                                        productId = candidateProduct;
                                        break;
                                }

                                if (flashItemId == null) {
                                        // Could not find a suitable flash item for this detail; skip
                                        continue;
                                }

                                // quantity: 1 or 2 (max 2 per flash sale rule), but cap by customer's remaining allowance
                                int desiredQuantity = 1 + random.nextInt(2);
                                String key = customerId + "_" + eventId + "_" + productId;
                                int currentCustomerQty = customerEventProductQty.getOrDefault(key, 0);
                                int remainingForCustomer = 2 - currentCustomerQty;
                                if (remainingForCustomer <= 0) {
                                        // shouldn't happen due to candidate filtering, but guard
                                        continue;
                                }

                                int currentSold = flashItemSoldQtyMap.getOrDefault(flashItemId, 0);
                                int limitedQty = flashItemLimitedQtyMap.get(flashItemId);
                                int stockRemaining = limitedQty - currentSold;
                                if (stockRemaining <= 0) {
                                        continue;
                                }

                                int actualQuantity = Math.min(desiredQuantity, Math.min(remainingForCustomer, stockRemaining));
                                if (actualQuantity <= 0) {
                                        continue;
                                }

                                // Now compute pricing based on actualQuantity
                                double unitPrice = flashItemPriceMap.get(flashItemId);
                                double subTotal = actualQuantity * unitPrice;
                                totalAmount += subTotal;

                                // Mark flashItem used in this order to avoid duplicates
                                usedFlashItems.add(flashItemId);

                                // Update customer's per-event-product quantity (applies to all orders generated)
                                customerEventProductQty.put(key, currentCustomerQty + actualQuantity);

                                // Accumulate soldQty for this flash item (only for SUCCESS orders)
                                if ("SUCCESS".equals(status)) {
                                        flashItemSoldQtyMap.put(flashItemId, currentSold + actualQuantity);
                                }

                                bwDetails.write(String.join(",",
                                                detailId,
                                                createdAt.format(formatter),
                                                updatedAt.format(formatter),
                                                orderId,
                                                flashItemId,
                                                String.valueOf(actualQuantity),
                                                String.valueOf((long) unitPrice),
                                                String.valueOf((long) subTotal)));
                                bwDetails.newLine();
                        }

                        // totalAmount = sum of all subTotals
                        // Accumulate customer spending (only for SUCCESS orders)
                        if ("SUCCESS".equals(status)) {
                                double currentSpent = customerSpentMap.getOrDefault(customerId, 0.0);
                                customerSpentMap.put(customerId, currentSpent + totalAmount);
                        }

                        // Store order info for Payment & OrderTransaction generation
                        orderCustomerMap.put(orderId, customerId);
                        orderEventMap.put(orderId, eventId);
                        orderTotalMap.put(orderId, totalAmount);
                        orderStatusMap.put(orderId, status);
                        orderLockMap.put(orderId, lock);

                        bwOrders.write(String.join(",",
                                        orderId,
                                        createdAt.format(formatter),
                                        updatedAt.format(formatter),
                                        customerId,
                                        eventId,
                                        String.valueOf((long) totalAmount),
                                        status,
                                        lock));
                        bwOrders.newLine();
                }

                bwOrders.close();
                bwDetails.close();
        }

        // =========================================================
        // REWRITE FLASH ITEMS with correct soldQty (Phase 5)
        // =========================================================

        private static void rewriteFlashItemsWithSoldQty() throws IOException {

                // Re-read and rewrite flash_items.csv with updated soldQty
                BufferedWriter bw = new BufferedWriter(new FileWriter("data/flash_items.csv"));

                bw.write("id,createdAt,updatedAt,eventId,productId,flashPrice,limitedQty,soldQty,discountPercent,version,status");
                bw.newLine();

                for (int i = 1; i <= NUM_FLASH_ITEMS; i++) {

                        String id = "FI" + String.format("%05d", i);

                        LocalDateTime createdAt = randomDateTime();
                        LocalDateTime updatedAt = randomUpdatedAt(createdAt);

                        // Find which event this item belongs to
                        String eventId = null;
                        for (Map.Entry<String, List<String>> entry : eventFlashItemsMap.entrySet()) {
                                if (entry.getValue().contains(id)) {
                                        eventId = entry.getKey();
                                        break;
                                }
                        }

                        String productId = flashItemProductMap.get(id);
                        double originalPrice = productPriceMap.get(productId);
                        double flashPrice = flashItemPriceMap.get(id);
                        int limitedQty = flashItemLimitedQtyMap.get(id);
                        int soldQty = flashItemSoldQtyMap.getOrDefault(id, 0);

                        // Recalculate discountPercent from prices
                        int discountPct = (int) Math.round((1.0 - flashPrice / originalPrice) * 100);

                        // Deterministic versioning: bump with sold quantities
                        int version = 1 + (soldQty / 10);

                        String status;
                        LocalDateTime eventEnd = eventEndMap.get(eventId);
                        LocalDateTime eventStart = eventStartMap.get(eventId);
                        LocalDateTime now = LocalDateTime.now();
                        if (now.isBefore(eventStart)) {
                                status = "UPCOMING";
                        } else if (now.isAfter(eventEnd)) {
                                status = "ENDED";
                        } else {
                                // If sold out, mark as DISABLED
                                if (soldQty >= limitedQty) {
                                        status = "DISABLED";
                                } else {
                                        status = "ACTIVE";
                                }
                        }

                        bw.write(String.join(",",
                                        id,
                                        createdAt.format(formatter),
                                        updatedAt.format(formatter),
                                        eventId,
                                        productId,
                                        String.valueOf((long) flashPrice),
                                        String.valueOf(limitedQty),
                                        String.valueOf(soldQty),
                                        String.valueOf(discountPct),
                                        String.valueOf(version),
                                        status));

                        bw.newLine();
                }

                bw.close();
        }

        // =========================================================
        // REWRITE CUSTOMERS with correct totalSpent and tier (Phase 6)
        // =========================================================

        private static void rewriteCustomersWithSpent() throws IOException {

                BufferedWriter bw = new BufferedWriter(new FileWriter("data/customers.csv"));

                bw.write("id,createdAt,updatedAt,userId,fullName,phone,email,tier,totalSpent,active");
                bw.newLine();

                for (int i = 1; i <= NUM_CUSTOMERS; i++) {

                        String id = "C" + String.format("%05d", i);
                        String userId = "U" + String.format("%05d", i);

                        LocalDateTime createdAt = randomDateTime();
                        LocalDateTime updatedAt = randomUpdatedAt(createdAt);

                        String fullName = randomFullName();
                        customerFullNameByUserId.put(userId, fullName);

                        String email = createEmail(fullName, i);

                        String phone = "09" + (10000000 + random.nextInt(89999999));

                        double totalSpent = customerSpentMap.getOrDefault(id, 0.0);

                        // Tier derived from totalSpent
                        String tier;
                        if (totalSpent >= 50000000) {
                                tier = "PREMIUM";
                        } else if (totalSpent >= 10000000) {
                                tier = "VIP";
                        } else {
                                tier = "NORMAL";
                        }

                        boolean active = random.nextInt(10) < 8; // 80% active

                        bw.write(String.join(",",
                                        id,
                                        createdAt.format(formatter),
                                        updatedAt.format(formatter),
                                        userId,
                                        fullName,
                                        phone,
                                        email,
                                        tier,
                                        String.valueOf((long) totalSpent),
                                        String.valueOf(active)));

                        bw.newLine();
                }

                bw.close();
        }

        // =========================================================
        // USERS
        // =========================================================

        private static void generateUsers(int count) throws IOException {

                BufferedWriter bw = new BufferedWriter(new FileWriter("data/users.csv"));

                bw.write("id,createdAt,updatedAt,username,passwordHash,role,active");
                bw.newLine();

                for (int i = 1; i <= count; i++) {

                        String id = "U" + String.format("%05d", i);

                        LocalDateTime createdAt = randomDateTime();
                        LocalDateTime updatedAt = randomUpdatedAt(createdAt);

                        String fullName = customerFullNameByUserId.get(id);
                        String username = createUsername(
                                        fullName != null ? fullName : randomFullName(),
                                        i);

                        // Simulated bcrypt-style hash
                        String passwordHash = "$2a$10$" + randomAlphanumeric(53);

                        String role;
                        if (i <= NUM_CUSTOMERS) {
                                role = "CUSTOMER";
                        } else {
                                role = random.nextInt(100) < 75 ? "SELLER" : "ADMIN";
                        }

                        boolean active = random.nextInt(10) < 9; // 90% active

                        bw.write(String.join(",",
                                        id,
                                        createdAt.format(formatter),
                                        updatedAt.format(formatter),
                                        username,
                                        passwordHash,
                                        role,
                                        String.valueOf(active)));

                        bw.newLine();
                }

                bw.close();
        }

        // =========================================================
        // PAYMENTS (one per SUCCESS order)
        // =========================================================

        private static void generatePayments() throws IOException {

                BufferedWriter bw = new BufferedWriter(new FileWriter("data/payments.csv"));

                bw.write("id,createdAt,updatedAt,orderId,customerId,paymentMethod,amount");
                bw.newLine();

                int paymentCounter = 0;

                for (Map.Entry<String, String> entry : orderStatusMap.entrySet()) {

                        String orderId = entry.getKey();
                        String status = entry.getValue();

                        // Only SUCCESS orders have payments
                        if (!"SUCCESS".equals(status)) {
                                continue;
                        }

                        paymentCounter++;
                        String id = "PAY" + String.format("%06d", paymentCounter);

                        // Ensure payment time is after order createdAt
                        LocalDateTime orderCreated = orderCreatedAtMap.getOrDefault(orderId, randomDateTime());
                        LocalDateTime paymentTime = orderCreated.plusMinutes(1 + random.nextInt(60 * 24));
                        LocalDateTime updatedAt = randomUpdatedAt(paymentTime);

                        String customerId = orderCustomerMap.get(orderId);
                        double amount = orderTotalMap.get(orderId);

                        String paymentMethod = PAYMENT_METHODS[random.nextInt(PAYMENT_METHODS.length)];

                        // Record payment time for transactions
                        orderPaymentTimeMap.put(orderId, paymentTime);

                        bw.write(String.join(",",
                                        id,
                                        paymentTime.format(formatter),
                                        updatedAt.format(formatter),
                                        orderId,
                                        customerId,
                                        paymentMethod,
                                        String.valueOf((long) amount)));

                        bw.newLine();
                }

                bw.close();
        }

        // =========================================================
        // ORDER TRANSACTIONS (one per order)
        // =========================================================

        private static void generateOrderTransactions() throws IOException {

                BufferedWriter bw = new BufferedWriter(new FileWriter("data/transactions.csv"));

                bw.write("id,createdAt,updatedAt,orderId,threadName,mechanism,success,retryCount,executionTimeMs,message");
                bw.newLine();

                int txCounter = 0;

                for (Map.Entry<String, String> entry : orderStatusMap.entrySet()) {

                        String orderId = entry.getKey();
                        String status = entry.getValue();

                        txCounter++;
                        String id = "TX" + String.format("%06d", txCounter);

                        // Prefer transaction time after payment, else after order createdAt
                        LocalDateTime baseTime = orderPaymentTimeMap.getOrDefault(orderId, orderCreatedAtMap.getOrDefault(orderId, randomDateTime()));
                        LocalDateTime createdAt = baseTime.plusSeconds(random.nextInt(3600));
                        LocalDateTime updatedAt = randomUpdatedAt(createdAt);

                        String threadName = THREAD_NAMES[random.nextInt(THREAD_NAMES.length)];

                        String mechanism = orderLockMap.get(orderId);

                        boolean success = "SUCCESS".equals(status);

                        int retryCount;
                        if (success) {
                                retryCount = random.nextInt(3); // 0-2 retries
                        } else {
                                retryCount = 1 + random.nextInt(5); // 1-5 retries
                        }

                        long executionTimeMs = 5 + random.nextInt(500); // 5-504 ms

                        String message;
                        if (success) {
                                message = TX_MESSAGES_SUCCESS[random.nextInt(TX_MESSAGES_SUCCESS.length)];
                        } else {
                                message = TX_MESSAGES_FAIL[random.nextInt(TX_MESSAGES_FAIL.length)];
                        }

                        bw.write(String.join(",",
                                        id,
                                        createdAt.format(formatter),
                                        updatedAt.format(formatter),
                                        orderId,
                                        threadName,
                                        mechanism,
                                        String.valueOf(success),
                                        String.valueOf(retryCount),
                                        String.valueOf(executionTimeMs),
                                        message));

                        bw.newLine();
                }

                bw.close();
        }

        // =========================================================
        // HELPER METHODS
        // =========================================================

        /**
         * Returns a random LocalDateTime within the past year.
         */
        private static LocalDateTime randomDateTime() {

                return LocalDateTime.now()
                                .minusDays(random.nextInt(365))
                                .minusHours(random.nextInt(24))
                                .minusMinutes(random.nextInt(60));
        }

        /**
         * Returns a random LocalDateTime that is always >= createdAt.
         * Ensures updatedAt >= createdAt.
         */
        private static LocalDateTime randomUpdatedAt(LocalDateTime createdAt) {

                // updatedAt = createdAt + 0 to 30 days + 0 to 23 hours
                return createdAt
                                .plusDays(random.nextInt(31))
                                .plusHours(random.nextInt(24))
                                .plusMinutes(random.nextInt(60));
        }

        /**
         * Picks a random event that has at least 1 flash item.
         */
        private static String pickEventWithItems() {

                List<String> eventsWithItems = new ArrayList<>();
                for (Map.Entry<String, List<String>> entry : eventFlashItemsMap.entrySet()) {
                        if (!entry.getValue().isEmpty()) {
                                eventsWithItems.add(entry.getKey());
                        }
                }

                if (eventsWithItems.isEmpty()) {
                        // Fallback: shouldn't happen with 1000 flash items across 50 events
                        return "E001";
                }

                return eventsWithItems.get(random.nextInt(eventsWithItems.size()));
        }

        private static String randomFullName() {

                return FIRST_NAMES[random.nextInt(FIRST_NAMES.length)] + " "
                                + MIDDLE_NAMES[random.nextInt(MIDDLE_NAMES.length)] + " "
                                + LAST_NAMES[random.nextInt(LAST_NAMES.length)];
        }

        private static String createEmail(String fullName, int number) {

                String email = fullName.toLowerCase()
                                .replace(" ", ".");

                return email + number + "@gmail.com";
        }

        private static String createUsername(String fullName, int number) {

                String[] parts = fullName.toLowerCase().split("\\s+");
                String firstName = parts[0];
                String lastName = parts[parts.length - 1];

                return firstName + "." + lastName + number;
        }

        /**
         * Generates a random alphanumeric string of the given length.
         * Used for simulating password hashes.
         */
        private static String randomAlphanumeric(int length) {

                String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789./";
                StringBuilder sb = new StringBuilder(length);
                for (int i = 0; i < length; i++) {
                        sb.append(chars.charAt(random.nextInt(chars.length())));
                }
                return sb.toString();
        }
}
