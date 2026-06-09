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

public class DataGenerator {

    private static final Random random = new Random();

    private static final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

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

            {"MacBook Air M2", "Dell XPS 15", "Asus ROG Zephyrus",
                    "Lenovo ThinkPad X1", "HP Pavilion Gaming"},

            {"iPhone 15 Pro Max", "Samsung Galaxy S25",
                    "Xiaomi 14 Ultra", "Oppo Find X7",
                    "Google Pixel 9"},

            {"iPad Air Gen 6", "Samsung Galaxy Tab S9",
                    "Xiaomi Pad 7", "Huawei MatePad Pro",
                    "Lenovo Tab Extreme"},

            {"PlayStation 5", "Xbox Series X",
                    "Nintendo Switch OLED", "MSI Gaming PC",
                    "Razer Blade 16"},

            {"Nike Air Force 1", "Adidas Ultraboost",
                    "Uniqlo Hoodie", "Levis 501 Jeans",
                    "Puma Running Shoes"},

            {"Dyson Vacuum V15", "Philips Air Fryer",
                    "Panasonic Refrigerator", "LG Washing Machine",
                    "Sharp Microwave Oven"},

            {"Canon EOS R6", "Sony A7 IV",
                    "Fujifilm XT5", "Nikon Z8",
                    "GoPro Hero 13"},

            {"Logitech MX Master 3S", "Anker PowerBank 20000mAh",
                    "UGreen USB-C Hub", "Baseus Charger 100W",
                    "Apple Magic Mouse"},

            {"Sony WH-1000XM6", "AirPods Pro 3",
                    "JBL Charge 6", "Marshall Stanmore",
                    "HyperX Cloud III"},

            {"Apple Watch Ultra 3", "Samsung Galaxy Watch 8",
                    "Garmin Forerunner 965", "Huawei Watch GT5",
                    "Xiaomi Watch S4"}
    };

    private static final String[] SALE_STATUS = {
            "UPCOMING", "ACTIVE", "ENDED", "DISABLED"
    };

    private static final String[] CUSTOMER_TIERS = {
            "NORMAL", "VIP", "PREMIUM"
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

    public static void main(String[] args) {

        new File("data").mkdirs();

        try {

            generateProducts(5000);
            generateFlashEvents(50);
            generateFlashItems(1000);
            generateCustomers(2500);
            generateOrders(3000);
            generateOrderDetails(3500);

            System.out.println("Generate CSV SUCCESS!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =========================================================
    // PRODUCTS
    // =========================================================

    private static void generateProducts(int count) throws IOException {

        BufferedWriter bw =
                new BufferedWriter(new FileWriter("data/products.csv"));

        bw.write("id,createdAt,updatedAt,name,category,originalPrice,stockQty,version,status");
        bw.newLine();

        for (int i = 1; i <= count; i++) {

            int categoryIndex = random.nextInt(CATEGORIES.length);

            String category = CATEGORIES[categoryIndex];

            String productName =
                    PRODUCT_NAMES[categoryIndex]
                            [random.nextInt(PRODUCT_NAMES[categoryIndex].length)];

            String fullProductName =
                    productName + " " +
                            (2023 + random.nextInt(4)) +
                            " Edition";

            String id = "P" + String.format("%05d", i);

            String createdAt = randomDate();
            String updatedAt = randomDate();

            double price =
                    500000 + random.nextInt(50000000);

            int stockQty =
                    100 + random.nextInt(5000);

            int version = random.nextInt(20);

            String status =
                    SALE_STATUS[random.nextInt(SALE_STATUS.length)];

            bw.write(String.join(",",
                    id,
                    createdAt,
                    updatedAt,
                    fullProductName,
                    category,
                    String.valueOf((long) price),
                    String.valueOf(stockQty),
                    String.valueOf(version),
                    status
            ));

            bw.newLine();
        }

        bw.close();
    }

    // =========================================================
    // FLASH EVENTS
    // =========================================================

    private static void generateFlashEvents(int count)
            throws IOException {

        BufferedWriter bw =
                new BufferedWriter(new FileWriter("data/flash_events.csv"));

        bw.write("id,createdAt,updatedAt,eventName,startTime,endTime,status");
        bw.newLine();

        for (int i = 1; i <= count; i++) {

            String id = "E" + String.format("%03d", i);

            String createdAt = randomDate();
            String updatedAt = randomDate();

            LocalDateTime start =
                    LocalDateTime.now().minusDays(random.nextInt(30));

            LocalDateTime end =
                    start.plusHours(2 + random.nextInt(5));

            String eventName =
                    "Mega Flash Sale " + (i);

            String status =
                    SALE_STATUS[random.nextInt(SALE_STATUS.length)];

            bw.write(String.join(",",
                    id,
                    createdAt,
                    updatedAt,
                    eventName,
                    start.format(formatter),
                    end.format(formatter),
                    status
            ));

            bw.newLine();
        }

        bw.close();
    }

    // =========================================================
    // FLASH ITEMS
    // =========================================================

    private static void generateFlashItems(int count)
            throws IOException {

        BufferedWriter bw =
                new BufferedWriter(new FileWriter("data/flash_items.csv"));

        bw.write("id,createdAt,updatedAt,eventId,productId,flashPrice,limitedQty,soldQty,version,status");
        bw.newLine();

        for (int i = 1; i <= count; i++) {

            String id = "FI" + String.format("%05d", i);

            String createdAt = randomDate();
            String updatedAt = randomDate();

            String eventId =
                    "E" + String.format("%03d",
                            1 + random.nextInt(50));

            String productId =
                    "P" + String.format("%05d",
                            1 + random.nextInt(5000));

            int limitedQty =
                    10 + random.nextInt(200);

            int soldQty =
                    random.nextInt(limitedQty + 1);

            double flashPrice =
                    100000 + random.nextInt(30000000);

            int version =
                    random.nextInt(10);

            String status =
                    SALE_STATUS[random.nextInt(SALE_STATUS.length)];

            bw.write(String.join(",",
                    id,
                    createdAt,
                    updatedAt,
                    eventId,
                    productId,
                    String.valueOf((long) flashPrice),
                    String.valueOf(limitedQty),
                    String.valueOf(soldQty),
                    String.valueOf(version),
                    status
            ));

            bw.newLine();
        }

        bw.close();
    }

    // =========================================================
    // CUSTOMERS
    // =========================================================

    private static void generateCustomers(int count)
            throws IOException {

        BufferedWriter bw =
                new BufferedWriter(new FileWriter("data/customers.csv"));

        bw.write("id,createdAt,updatedAt,fullName,phone,email,tier,totalSpent,active");
        bw.newLine();

        for (int i = 1; i <= count; i++) {

            String id = "C" + String.format("%05d", i);

            String createdAt = randomDate();
            String updatedAt = randomDate();

            String fullName = randomFullName();

            String email =
                    createEmail(fullName, i);

            String phone =
                    "09" + (10000000 + random.nextInt(89999999));

            String tier =
                    CUSTOMER_TIERS[random.nextInt(CUSTOMER_TIERS.length)];

            double totalSpent =
                    random.nextInt(200000000);

            boolean active =
                    random.nextBoolean();

            bw.write(String.join(",",
                    id,
                    createdAt,
                    updatedAt,
                    fullName,
                    phone,
                    email,
                    tier,
                    String.valueOf((long) totalSpent),
                    String.valueOf(active)
            ));

            bw.newLine();
        }

        bw.close();
    }

    // =========================================================
    // ORDERS
    // =========================================================

    private static void generateOrders(int count)
            throws IOException {

        BufferedWriter bw =
                new BufferedWriter(new FileWriter("data/orders.csv"));

        bw.write("id,createdAt,updatedAt,customerId,eventId,totalAmount,status,lockMechanism");
        bw.newLine();

        for (int i = 1; i <= count; i++) {

            String id = "O" + String.format("%06d", i);

            String createdAt = randomDate();
            String updatedAt = randomDate();

            String customerId =
                    "C" + String.format("%05d",
                            1 + random.nextInt(2500));

            String eventId =
                    "E" + String.format("%03d",
                            1 + random.nextInt(50));

            double totalAmount =
                    100000 + random.nextInt(50000000);

            String status =
                    ORDER_STATUS[random.nextInt(ORDER_STATUS.length)];

            String lock =
                    LOCKS[random.nextInt(LOCKS.length)];

            bw.write(String.join(",",
                    id,
                    createdAt,
                    updatedAt,
                    customerId,
                    eventId,
                    String.valueOf((long) totalAmount),
                    status,
                    lock
            ));

            bw.newLine();
        }

        bw.close();
    }

    // =========================================================
    // ORDER DETAILS
    // =========================================================

    private static void generateOrderDetails(int count)
            throws IOException {

        BufferedWriter bw =
                new BufferedWriter(new FileWriter("data/order_details.csv"));

        bw.write("id,createdAt,updatedAt,orderId,flashItemId,quantity,unitPrice,subTotal");
        bw.newLine();

        for (int i = 1; i <= count; i++) {

            String id = "OD" + String.format("%06d", i);

            String createdAt = randomDate();
            String updatedAt = randomDate();

            String orderId =
                    "O" + String.format("%06d",
                            1 + random.nextInt(3000));

            String flashItemId =
                    "FI" + String.format("%05d",
                            1 + random.nextInt(1000));

            int quantity =
                    1 + random.nextInt(2);

            double unitPrice =
                    100000 + random.nextInt(10000000);

            double subTotal =
                    quantity * unitPrice;

            bw.write(String.join(",",
                    id,
                    createdAt,
                    updatedAt,
                    orderId,
                    flashItemId,
                    String.valueOf(quantity),
                    String.valueOf((long) unitPrice),
                    String.valueOf((long) subTotal)
            ));

            bw.newLine();
        }

        bw.close();
    }

    // =========================================================
    // HELPER METHODS
    // =========================================================

    private static String randomDate() {

        LocalDateTime date =
                LocalDateTime.now()
                        .minusDays(random.nextInt(365))
                        .minusHours(random.nextInt(24))
                        .minusMinutes(random.nextInt(60));

        return date.format(formatter);
    }

    private static String randomFullName() {

        return FIRST_NAMES[random.nextInt(FIRST_NAMES.length)] + " "
                + MIDDLE_NAMES[random.nextInt(MIDDLE_NAMES.length)] + " "
                + LAST_NAMES[random.nextInt(LAST_NAMES.length)];
    }

    private static String createEmail(String fullName, int number) {

        String email =
                fullName.toLowerCase()
                        .replace(" ", ".");

        return email + number + "@gmail.com";
    }
}
