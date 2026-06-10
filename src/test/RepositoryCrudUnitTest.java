
import model.BaseEntity.BaseEntity;
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
import exception.OutOfStockException;
import repository.CsvRepository;
import repository.CustomerRepository;
import repository.FlashSaleItemRepository;
import repository.FlashSaleRepository;
import repository.OrderDetailRepository;
import repository.OrderRepository;
import repository.OrderTransactionRepository;
import repository.PaymentRepository;
import repository.ProductRepository;
import repository.UserRepository;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.DirectoryStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

public class RepositoryCrudUnitTest {

    private static final LocalDateTime CREATED_AT =
            LocalDateTime.of(2026, 1, 2, 3, 4, 5);
    private static final LocalDateTime UPDATED_AT =
            LocalDateTime.of(2026, 2, 3, 4, 5, 6);
    private static final Path TEST_DIR =
            Paths.get("data", "test_repository_crud");

    private static int totalTests = 0;
    private static int passedTests = 0;

    public static void main(String[] args) throws Exception {
        if (args.length > 0 && "--test".equals(args[0])) {
            runAutomatedTests();
            return;
        }

        runConsoleCrudDemo();
    }

    private static void runAutomatedTests() throws Exception {
        Files.createDirectories(TEST_DIR);

        try {
            runTest("CustomerRepository CRUD", RepositoryCrudUnitTest::testCustomerRepositoryCrud);
            runTest("ProductRepository CRUD", RepositoryCrudUnitTest::testProductRepositoryCrud);
            runTest("FlashSaleRepository CRUD", RepositoryCrudUnitTest::testFlashSaleRepositoryCrud);
            runTest("FlashSaleItemRepository CRUD", RepositoryCrudUnitTest::testFlashSaleItemRepositoryCrud);
            runTest("FlashSaleItemRepository special sell methods",
                    RepositoryCrudUnitTest::testFlashSaleItemRepositorySpecialSellMethods);
            runTest("OrderRepository CRUD", RepositoryCrudUnitTest::testOrderRepositoryCrud);
            runTest("OrderDetailRepository CRUD", RepositoryCrudUnitTest::testOrderDetailRepositoryCrud);
            runTest("PaymentRepository CRUD", RepositoryCrudUnitTest::testPaymentRepositoryCrud);
            runTest("OrderTransactionRepository CRUD", RepositoryCrudUnitTest::testOrderTransactionRepositoryCrud);
            runTest("UserRepository CRUD", RepositoryCrudUnitTest::testUserRepositoryCrud);
            runTest("Read 10k rows under 1 second", RepositoryCrudUnitTest::testReadTenThousandRowsUnderOneSecond);

            System.out.println("========================================");
            System.out.println("SUMMARY: " + passedTests + "/" + totalTests + " tests passed.");

            if (passedTests != totalTests) {
                throw new AssertionError("Some repository CRUD unit tests failed.");
            }
        } finally {
            cleanupTestFiles();
        }
    }

    private static void runConsoleCrudDemo() throws Exception {
        Files.createDirectories(TEST_DIR);

        List<RepositoryConsole> repositories = createConsoleRepositories();
        Scanner scanner = new Scanner(System.in);

        System.out.println("========================================");
        System.out.println("DEMO CRUD TAT CA REPOSITORY");
        System.out.println("Du lieu demo nam trong: " + TEST_DIR);
        System.out.println("Data goc trong data/*.csv khong bi thay doi.");

        try {
            boolean running = true;
            while (running) {
                loadAndShowAllRepositories(repositories);

                System.out.println("========================================");
                System.out.println("CHON CHUC NANG CRUD");
                System.out.println("1. Them doi tuong");
                System.out.println("2. Sua doi tuong");
                System.out.println("3. Xoa doi tuong");
                System.out.println("4. Xem du lieu theo doi tuong");
                System.out.println("0. Thoat");
                System.out.print("Nhap lua chon: ");

                String choice = scanner.nextLine().trim();

                switch (choice) {
                    case "1":
                        addEntity(repositories, scanner);
                        break;
                    case "2":
                        updateEntity(repositories, scanner);
                        break;
                    case "3":
                        deleteEntity(repositories, scanner);
                        break;
                    case "4":
                        showRepositoryRows(repositories, scanner);
                        break;
                    case "0":
                        running = false;
                        System.out.println("Ket thuc demo CRUD.");
                        break;
                    default:
                        System.out.println("Lua chon khong hop le.");
                        break;
                }
            }
        } finally {
            repositories.clear();
            System.gc();
            sleepQuietly(200);
            cleanupTestFiles();
        }
    }

    private static List<RepositoryConsole> createConsoleRepositories()
            throws IOException {

        List<RepositoryConsole> repositories = new java.util.ArrayList<>();

        repositories.add(new RepositoryConsole(
                "1",
                "Customer",
                "customers.csv",
                "id,createdAt,updatedAt,fullName,phone,email,tier,totalSpent,active",
                new CustomerRepository(copyToDemoFile("customers.csv"))));

        repositories.add(new RepositoryConsole(
                "2",
                "Product",
                "products.csv",
                "id,createdAt,updatedAt,name,category,originalPrice,stockQty,version,status",
                new ProductRepository(copyToDemoFile("products.csv"))));

        repositories.add(new RepositoryConsole(
                "3",
                "FlashSaleEvent",
                "flash_events.csv",
                "id,createdAt,updatedAt,eventName,startTime,endTime,status",
                new FlashSaleRepository(copyToDemoFile("flash_events.csv"))));

        repositories.add(new RepositoryConsole(
                "4",
                "FlashSaleItem",
                "flash_items.csv",
                "id,createdAt,updatedAt,eventId,productId,flashPrice,limitedQty,soldQty,discountPercent,version,status",
                new FlashSaleItemRepository(copyToDemoFile("flash_items.csv"))));

        repositories.add(new RepositoryConsole(
                "5",
                "Order",
                "orders.csv",
                "id,createdAt,updatedAt,customerId,eventId,totalAmount,status,lockMechanism",
                new OrderRepository(copyToDemoFile("orders.csv"))));

        repositories.add(new RepositoryConsole(
                "6",
                "OrderDetail",
                "order_details.csv",
                "id,createdAt,updatedAt,orderId,flashItemId,quantity,unitPrice,subTotal",
                new OrderDetailRepository(copyToDemoFile("order_details.csv"))));

        repositories.add(new RepositoryConsole(
                "7",
                "Payment",
                "payments.csv",
                "id,createdAt,updatedAt,orderId,customerId,paymentMethod,amount",
                new PaymentRepository(copyToDemoFile("payments.csv"))));

        repositories.add(new RepositoryConsole(
                "8",
                "OrderTransaction",
                "transactions.csv",
                "id,createdAt,updatedAt,orderId,threadName,mechanism,success,retryCount,executionTimeMs,message",
                new OrderTransactionRepository(copyToDemoFile("transactions.csv"))));

        repositories.add(new RepositoryConsole(
                "9",
                "User",
                "users.csv",
                "id,createdAt,updatedAt,username,passwordHash,role,active",
                new UserRepository(copyToDemoFile("users.csv"))));

        return repositories;
    }

    private static String copyToDemoFile(String fileName) throws IOException {
        Path sourceFile = Paths.get("data", fileName);
        Path demoFile = TEST_DIR.resolve("console_" + fileName);

        if (Files.exists(sourceFile)) {
            Files.copy(sourceFile, demoFile, StandardCopyOption.REPLACE_EXISTING);
        }

        return demoFile.toString();
    }

    private static void loadAndShowAllRepositories(
            List<RepositoryConsole> repositories) {

        int totalRows = 0;
        long totalElapsedNs = 0L;

        System.out.println("========================================");
        System.out.println("NAP TAT CA CSV VA DO THOI GIAN DOC");
        System.out.println("----------------------------------------");

        for (RepositoryConsole context : repositories) {
            long startNs = System.nanoTime();
            List rows = context.repository.findAll();
            long elapsedNs = System.nanoTime() - startNs;

            int rowCount = rows.size();
            long elapsedMs = elapsedNs / 1_000_000;

            totalRows += rowCount;
            totalElapsedNs += elapsedNs;

            System.out.println(context.key + ". "
                    + padRight(context.displayName, 18)
                    + " | file: " + padRight(context.fileName, 18)
                    + " | rows: " + rowCount
                    + " | ms: " + elapsedMs);
        }

        double rowsPerSecond = totalRows == 0
                ? 0.0
                : totalRows / (totalElapsedNs / 1_000_000_000.0);

        System.out.println("----------------------------------------");
        System.out.println("Tong so dong     : " + totalRows);
        System.out.println("Tong thoi gian ms: " + (totalElapsedNs / 1_000_000));
        System.out.println("Toc do dong/giay : " + String.format("%.2f", rowsPerSecond));
    }

    private static void addEntity(
            List<RepositoryConsole> repositories,
            Scanner scanner) {

        System.out.println("========================================");
        System.out.println("THEM DOI TUONG");

        RepositoryConsole context = chooseRepository(repositories, scanner);
        if (context == null) {
            return;
        }

        BaseEntity entity = buildEntity(context.displayName, scanner, null);

        if (context.repository.findById(entity.getId()) != null) {
            System.out.println("ID da ton tai, khong the them.");
            return;
        }

        context.repository.save(entity);
        System.out.println("Da them " + context.displayName + ": " + entity.toCsvLine());
    }

    private static void updateEntity(
            List<RepositoryConsole> repositories,
            Scanner scanner) {

        System.out.println("========================================");
        System.out.println("SUA DOI TUONG");

        RepositoryConsole context = chooseRepository(repositories, scanner);
        if (context == null) {
            return;
        }

        String id = prompt(scanner, "Nhap ID can sua");
        BaseEntity current = context.repository.findById(id);

        if (current == null) {
            System.out.println("Khong tim thay " + context.displayName + " co ID: " + id);
            return;
        }

        System.out.println("Du lieu hien tai: " + current.toCsvLine());
        System.out.println("Nhan Enter de giu nguyen gia tri cu.");

        BaseEntity updated = buildEntity(context.displayName, scanner, current);

        boolean success = context.repository.update(updated);
        System.out.println(success
                ? "Da sua: " + updated.toCsvLine()
                : "Sua that bai.");
    }

    private static void deleteEntity(
            List<RepositoryConsole> repositories,
            Scanner scanner)
            throws IOException {

        System.out.println("========================================");
        System.out.println("XOA DOI TUONG");

        RepositoryConsole context = chooseRepository(repositories, scanner);
        if (context == null) {
            return;
        }

        String id = prompt(scanner, "Nhap ID can xoa");
        BaseEntity current = context.repository.findById(id);

        if (current == null) {
            System.out.println("Khong tim thay " + context.displayName + " co ID: " + id);
            return;
        }

        System.out.println("Du lieu se xoa: " + current.toCsvLine());
        String confirm = prompt(scanner, "Nhap YES de xac nhan xoa");

        if (!"YES".equals(confirm)) {
            System.out.println("Da huy xoa.");
            return;
        }

        boolean success = context.repository.delete(id);
        System.out.println(success ? "Da xoa ID: " + id : "Xoa that bai.");
    }

    private static void showRepositoryRows(
            List<RepositoryConsole> repositories,
            Scanner scanner) {

        System.out.println("========================================");
        System.out.println("XEM DU LIEU THEO DOI TUONG");

        RepositoryConsole context = chooseRepository(repositories, scanner);
        if (context == null) {
            return;
        }

        int limit = promptInt(scanner, "So dong muon hien", 10);
        List rows = context.repository.findAll();

        System.out.println("Header: " + context.header);
        System.out.println("Tong dong: " + rows.size());
        System.out.println("----------------------------------------");

        for (int i = 0; i < Math.min(limit, rows.size()); i++) {
            BaseEntity entity = (BaseEntity) rows.get(i);
            System.out.println((i + 1) + ". " + entity.toCsvLine());
        }

        if (rows.isEmpty()) {
            System.out.println("(Chua co du lieu)");
        }
    }

    private static RepositoryConsole chooseRepository(
            List<RepositoryConsole> repositories,
            Scanner scanner) {

        System.out.println("----------------------------------------");
        System.out.println("CHON DOI TUONG");
        for (RepositoryConsole context : repositories) {
            System.out.println(context.key + ". " + context.displayName);
        }
        System.out.println("0. Quay lai");
        System.out.print("Nhap lua chon doi tuong: ");

        String choice = scanner.nextLine().trim();

        if ("0".equals(choice)) {
            return null;
        }

        for (RepositoryConsole context : repositories) {
            if (context.key.equals(choice)) {
                return context;
            }
        }

        System.out.println("Lua chon doi tuong khong hop le.");
        return null;
    }

    private static BaseEntity buildEntity(
            String entityName,
            Scanner scanner,
            BaseEntity current) {

        String id = current == null
                ? prompt(scanner, "ID")
                : current.getId();
        LocalDateTime createdAt = current == null
                ? LocalDateTime.now()
                : current.getCreatedAt();
        LocalDateTime updatedAt = LocalDateTime.now();

        switch (entityName) {
            case "Customer": {
                Customer old = (Customer) current;
                return new Customer(
                        id,
                        createdAt,
                        updatedAt,
                        promptOrDefault(scanner, "Ho ten", old == null ? "" : old.getFullName()),
                        promptOrDefault(scanner, "So dien thoai", old == null ? "" : old.getPhone()),
                        promptOrDefault(scanner, "Email", old == null ? "" : old.getEmail()),
                        promptTier(scanner, "Tier (NORMAL/VIP/PREMIUM)",
                                old == null ? CustomerTier.NORMAL : old.getTier()),
                        promptDouble(scanner, "Tong chi tieu",
                                old == null ? 0.0 : old.getTotalSpent()),
                        promptBoolean(scanner, "Active (true/false)",
                                old == null || old.isActive()));
            }
            case "Product": {
                Product old = (Product) current;
                return new Product(
                        id,
                        createdAt,
                        updatedAt,
                        promptOrDefault(scanner, "Ten san pham", old == null ? "" : old.getName()),
                        promptOrDefault(scanner, "Danh muc", old == null ? "" : old.getCategory()),
                        promptDouble(scanner, "Gia goc",
                                old == null ? 0.0 : old.getOriginalPrice()),
                        promptInt(scanner, "So luong ton",
                                old == null ? 0 : old.getStockQty()),
                        promptInt(scanner, "Version",
                                old == null ? 1 : old.getVersion()),
                        promptSaleStatus(scanner, "Status (UPCOMING/ACTIVE/ENDED/DISABLED)",
                                old == null ? SaleStatus.ACTIVE : old.getStatus()));
            }
            case "FlashSaleEvent": {
                FlashSaleEvent old = (FlashSaleEvent) current;
                return new FlashSaleEvent(
                        id,
                        createdAt,
                        updatedAt,
                        promptOrDefault(scanner, "Ten event", old == null ? "" : old.getEventName()),
                        promptDateTime(scanner, "Start time yyyy-MM-ddTHH:mm:ss",
                                old == null ? LocalDateTime.now() : old.getStartTime()),
                        promptDateTime(scanner, "End time yyyy-MM-ddTHH:mm:ss",
                                old == null ? LocalDateTime.now().plusHours(2) : old.getEndTime()),
                        promptSaleStatus(scanner, "Status (UPCOMING/ACTIVE/ENDED/DISABLED)",
                                old == null ? SaleStatus.UPCOMING : old.getStatus()));
            }
            case "FlashSaleItem": {
                FlashSaleItem old = (FlashSaleItem) current;
                return new FlashSaleItem(
                        id,
                        createdAt,
                        updatedAt,
                        promptOrDefault(scanner, "Event ID", old == null ? "" : old.getEventId()),
                        promptOrDefault(scanner, "Product ID", old == null ? "" : old.getProductId()),
                        promptDouble(scanner, "Flash price",
                                old == null ? 0.0 : old.getFlashPrice()),
                        promptInt(scanner, "Limited quantity",
                                old == null ? 0 : old.getLimitedQty()),
                        promptInt(scanner, "Sold quantity",
                                old == null ? 0 : old.getSoldQty()),
                        promptDouble(scanner, "Discount percent",
                                old == null ? 0.0 : old.getDiscountPercent()),
                        promptInt(scanner, "Version",
                                old == null ? 1 : old.getVersion()),
                        promptSaleStatus(scanner, "Status (UPCOMING/ACTIVE/ENDED/DISABLED)",
                                old == null ? SaleStatus.ACTIVE : old.getStatus()));
            }
            case "Order": {
                Order old = (Order) current;
                return new Order(
                        id,
                        createdAt,
                        updatedAt,
                        promptOrDefault(scanner, "Customer ID", old == null ? "" : old.getCustomerId()),
                        promptOrDefault(scanner, "Event ID", old == null ? "" : old.getEventId()),
                        promptDouble(scanner, "Total amount",
                                old == null ? 0.0 : old.getTotalAmount()),
                        promptOrderStatus(scanner, "Status (PENDING/SUCCESS/FAILED/CANCELLED)",
                                old == null ? OrderStatus.PENDING : old.getStatus()),
                        promptLockMechanism(scanner, "Lock (NO_LOCK/SYNCHRONIZED/FILE_LOCK/OPTIMISTIC_LOCK)",
                                old == null ? LockMechanism.NO_LOCK : old.getLockMechanism()));
            }
            case "OrderDetail": {
                OrderDetail old = (OrderDetail) current;
                return new OrderDetail(
                        id,
                        createdAt,
                        updatedAt,
                        promptOrDefault(scanner, "Order ID", old == null ? "" : old.getOrderId()),
                        promptOrDefault(scanner, "Flash item ID", old == null ? "" : old.getFlashItemId()),
                        promptInt(scanner, "Quantity",
                                old == null ? 1 : old.getQuantity()),
                        promptDouble(scanner, "Unit price",
                                old == null ? 0.0 : old.getUnitPrice()),
                        promptDouble(scanner, "Sub total",
                                old == null ? 0.0 : old.getSubTotal()));
            }
            case "Payment": {
                Payment old = (Payment) current;
                return new Payment(
                        id,
                        createdAt,
                        updatedAt,
                        promptOrDefault(scanner, "Order ID", old == null ? "" : old.getOrderId()),
                        promptOrDefault(scanner, "Customer ID", old == null ? "" : old.getCustomerId()),
                        promptPaymentMethod(scanner, "Payment method (CASH/BANKING)",
                                old == null ? PaymentMethod.CASH : old.getPaymentMethod()),
                        promptDouble(scanner, "Amount",
                                old == null ? 0.0 : old.getAmount()));
            }
            case "OrderTransaction": {
                OrderTransaction old = (OrderTransaction) current;
                return new OrderTransaction(
                        id,
                        createdAt,
                        updatedAt,
                        promptOrDefault(scanner, "Order ID", old == null ? "" : old.getOrderId()),
                        promptOrDefault(scanner, "Thread name", old == null ? "" : old.getThreadName()),
                        promptLockMechanism(scanner, "Lock (NO_LOCK/SYNCHRONIZED/FILE_LOCK/OPTIMISTIC_LOCK)",
                                old == null ? LockMechanism.NO_LOCK : old.getMechanism()),
                        promptBoolean(scanner, "Success (true/false)",
                                old != null && old.isSuccess()),
                        promptInt(scanner, "Retry count",
                                old == null ? 0 : old.getRetryCount()),
                        promptLong(scanner, "Execution time ms",
                                old == null ? 0L : old.getExecutionTimeMs()),
                        promptOrDefault(scanner, "Message", old == null ? "" : old.getMessage()));
            }
            case "User": {
                User old = (User) current;
                return new User(
                        id,
                        createdAt,
                        updatedAt,
                        promptOrDefault(scanner, "Username", old == null ? "" : old.getUsername()),
                        promptOrDefault(scanner, "Password hash", old == null ? "" : old.getPasswordHash()),
                        promptUserRole(scanner, "Role (CUSTOMER/SELLER/ADMIN)",
                                old == null ? UserRole.CUSTOMER : old.getRole()),
                        promptBoolean(scanner, "Active (true/false)",
                                old == null || old.isActive()));
            }
            default:
                throw new IllegalArgumentException("Unsupported entity: " + entityName);
        }
    }

    private static int promptInt(
            Scanner scanner,
            String label,
            int defaultValue) {

        while (true) {
            System.out.print(label + " [" + defaultValue + "]: ");
            String value = scanner.nextLine().trim();

            if (value.isEmpty()) {
                return defaultValue;
            }

            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                System.out.println("Vui long nhap so nguyen hop le.");
            }
        }
    }

    private static long promptLong(
            Scanner scanner,
            String label,
            long defaultValue) {

        while (true) {
            System.out.print(label + " [" + defaultValue + "]: ");
            String value = scanner.nextLine().trim();

            if (value.isEmpty()) {
                return defaultValue;
            }

            try {
                return Long.parseLong(value);
            } catch (NumberFormatException e) {
                System.out.println("Vui long nhap so nguyen hop le.");
            }
        }
    }

    private static LocalDateTime promptDateTime(
            Scanner scanner,
            String label,
            LocalDateTime defaultValue) {

        while (true) {
            System.out.print(label + " [" + defaultValue + "]: ");
            String value = scanner.nextLine().trim();

            if (value.isEmpty()) {
                return defaultValue;
            }

            try {
                return LocalDateTime.parse(value);
            } catch (Exception e) {
                System.out.println("Vui long nhap theo ISO, vi du: 2026-06-10T09:30:00");
            }
        }
    }

    private static SaleStatus promptSaleStatus(
            Scanner scanner,
            String label,
            SaleStatus defaultValue) {

        while (true) {
            System.out.print(label + " [" + defaultValue + "]: ");
            String value = scanner.nextLine().trim();

            if (value.isEmpty()) {
                return defaultValue;
            }

            try {
                return SaleStatus.valueOf(value.toUpperCase());
            } catch (IllegalArgumentException e) {
                System.out.println("Status khong hop le.");
            }
        }
    }

    private static OrderStatus promptOrderStatus(
            Scanner scanner,
            String label,
            OrderStatus defaultValue) {

        while (true) {
            System.out.print(label + " [" + defaultValue + "]: ");
            String value = scanner.nextLine().trim();

            if (value.isEmpty()) {
                return defaultValue;
            }

            try {
                return OrderStatus.valueOf(value.toUpperCase());
            } catch (IllegalArgumentException e) {
                System.out.println("Order status khong hop le.");
            }
        }
    }

    private static LockMechanism promptLockMechanism(
            Scanner scanner,
            String label,
            LockMechanism defaultValue) {

        while (true) {
            System.out.print(label + " [" + defaultValue + "]: ");
            String value = scanner.nextLine().trim();

            if (value.isEmpty()) {
                return defaultValue;
            }

            try {
                return LockMechanism.valueOf(value.toUpperCase());
            } catch (IllegalArgumentException e) {
                System.out.println("Lock mechanism khong hop le.");
            }
        }
    }

    private static PaymentMethod promptPaymentMethod(
            Scanner scanner,
            String label,
            PaymentMethod defaultValue) {

        while (true) {
            System.out.print(label + " [" + defaultValue + "]: ");
            String value = scanner.nextLine().trim();

            if (value.isEmpty()) {
                return defaultValue;
            }

            try {
                return PaymentMethod.valueOf(value.toUpperCase());
            } catch (IllegalArgumentException e) {
                System.out.println("Payment method khong hop le.");
            }
        }
    }

    private static UserRole promptUserRole(
            Scanner scanner,
            String label,
            UserRole defaultValue) {

        while (true) {
            System.out.print(label + " [" + defaultValue + "]: ");
            String value = scanner.nextLine().trim();

            if (value.isEmpty()) {
                return defaultValue;
            }

            try {
                return UserRole.valueOf(value.toUpperCase());
            } catch (IllegalArgumentException e) {
                System.out.println("User role khong hop le.");
            }
        }
    }

    private static String padRight(String value, int length) {
        if (value.length() >= length) {
            return value;
        }

        StringBuilder builder = new StringBuilder(value);
        while (builder.length() < length) {
            builder.append(' ');
        }

        return builder.toString();
    }

    private static String prompt(Scanner scanner, String label) {
        String value;
        do {
            System.out.print(label + ": ");
            value = scanner.nextLine().trim();
        } while (value.isEmpty());

        return value;
    }

    private static String promptOrDefault(
            Scanner scanner,
            String label,
            String defaultValue) {

        System.out.print(label + " [" + defaultValue + "]: ");
        String value = scanner.nextLine().trim();
        return value.isEmpty() ? defaultValue : value;
    }

    private static CustomerTier promptTier(
            Scanner scanner,
            String label,
            CustomerTier defaultValue) {

        while (true) {
            System.out.print(label + " [" + defaultValue + "]: ");
            String value = scanner.nextLine().trim();

            if (value.isEmpty()) {
                return defaultValue;
            }

            try {
                return CustomerTier.valueOf(value.toUpperCase());
            } catch (IllegalArgumentException e) {
                System.out.println("Tier khong hop le.");
            }
        }
    }

    private static double promptDouble(
            Scanner scanner,
            String label,
            double defaultValue) {

        while (true) {
            System.out.print(label + " [" + defaultValue + "]: ");
            String value = scanner.nextLine().trim();

            if (value.isEmpty()) {
                return defaultValue;
            }

            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
                System.out.println("Vui long nhap so hop le.");
            }
        }
    }

    private static boolean promptBoolean(
            Scanner scanner,
            String label,
            boolean defaultValue) {

        while (true) {
            System.out.print(label + " [" + defaultValue + "]: ");
            String value = scanner.nextLine().trim();

            if (value.isEmpty()) {
                return defaultValue;
            }

            if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
                return Boolean.parseBoolean(value);
            }

            System.out.println("Vui long nhap true hoac false.");
        }
    }

    private static void testCustomerRepositoryCrud() throws Exception {
        runCrudAssertions(
                new CustomerRepository(testFile("customers.csv")),
                testFile("customers.csv"),
                "id,createdAt,updatedAt,fullName,phone,email,tier,totalSpent,active",
                new Customer("C001", CREATED_AT, UPDATED_AT,
                        "Nguyen Van A", "0909123456", "a@example.com",
                        CustomerTier.NORMAL, 1000.0, true),
                new Customer("C002", CREATED_AT, UPDATED_AT,
                        "Tran Thi B", "0911222333", "b@example.com",
                        CustomerTier.VIP, 2000.0, true),
                new Customer("C002", CREATED_AT, UPDATED_AT,
                        "Tran Thi B Updated", "0911222333", "b@example.com",
                        CustomerTier.PREMIUM, 3000.0, false));
    }

    private static void testProductRepositoryCrud() throws Exception {
        runCrudAssertions(
                new ProductRepository(testFile("products.csv")),
                testFile("products.csv"),
                "id,createdAt,updatedAt,name,category,originalPrice,stockQty,version,status",
                new Product("P001", CREATED_AT, UPDATED_AT,
                        "Laptop A", "Laptop", 1000.0, 20, 1,
                        SaleStatus.ACTIVE),
                new Product("P002", CREATED_AT, UPDATED_AT,
                        "Phone B", "Smartphone", 2000.0, 30, 1,
                        SaleStatus.ACTIVE),
                new Product("P002", CREATED_AT, UPDATED_AT,
                        "Phone B Updated", "Smartphone", 2500.0, 25, 2,
                        SaleStatus.DISABLED));
    }

    private static void testFlashSaleRepositoryCrud() throws Exception {
        runCrudAssertions(
                new FlashSaleRepository(testFile("flash_events.csv")),
                testFile("flash_events.csv"),
                "id,createdAt,updatedAt,eventName,startTime,endTime,status",
                new FlashSaleEvent("E001", CREATED_AT, UPDATED_AT,
                        "Morning Sale",
                        LocalDateTime.of(2026, 3, 1, 8, 0, 1),
                        LocalDateTime.of(2026, 3, 1, 10, 0, 1),
                        SaleStatus.UPCOMING),
                new FlashSaleEvent("E002", CREATED_AT, UPDATED_AT,
                        "Noon Sale",
                        LocalDateTime.of(2026, 3, 2, 12, 0, 1),
                        LocalDateTime.of(2026, 3, 2, 14, 0, 1),
                        SaleStatus.ACTIVE),
                new FlashSaleEvent("E002", CREATED_AT, UPDATED_AT,
                        "Noon Sale Updated",
                        LocalDateTime.of(2026, 3, 2, 12, 0, 1),
                        LocalDateTime.of(2026, 3, 2, 15, 0, 1),
                        SaleStatus.ENDED));
    }

    private static void testFlashSaleItemRepositoryCrud() throws Exception {
        runCrudAssertions(
                new FlashSaleItemRepository(testFile("flash_items.csv")),
                testFile("flash_items.csv"),
                "id,createdAt,updatedAt,eventId,productId,flashPrice,limitedQty,soldQty,discountPercent,version,status",
                new FlashSaleItem("FI001", CREATED_AT, UPDATED_AT,
                        "E001", "P001", 800.0, 100, 0, 20.0, 1,
                        SaleStatus.ACTIVE),
                new FlashSaleItem("FI002", CREATED_AT, UPDATED_AT,
                        "E001", "P002", 1500.0, 50, 5, 25.0, 1,
                        SaleStatus.ACTIVE),
                new FlashSaleItem("FI002", CREATED_AT, UPDATED_AT,
                        "E001", "P002", 1400.0, 50, 10, 30.0, 2,
                        SaleStatus.ACTIVE));
    }

    private static void testFlashSaleItemRepositorySpecialSellMethods()
            throws Exception {

        assertSellWithNoLockUpdatesSoldQty();
        assertSellWithSynchronizedUpdatesSoldQty();
        assertSellWithFileLockUpdatesSoldQty();
        assertSellWithOptimisticLockUpdatesSoldQtyAndVersion();
        assertSellThrowsOutOfStockAndKeepsData();
    }

    private static void assertSellWithNoLockUpdatesSoldQty() throws Exception {
        FlashSaleItemRepository repository =
                prepareFlashSaleItemRepository("flash_items_no_lock.csv", 2, 10, 1);

        assertTrue(repository.sellWithNoLock("FI_SPECIAL", 3),
                "sellWithNoLock should return true");

        FlashSaleItem item = repository.findById("FI_SPECIAL");
        assertEquals(5, item.getSoldQty());
        assertEquals(1, item.getVersion());
    }

    private static void assertSellWithSynchronizedUpdatesSoldQty()
            throws Exception {

        FlashSaleItemRepository repository =
                prepareFlashSaleItemRepository("flash_items_synchronized.csv", 1, 10, 1);

        assertTrue(repository.sellWithSynchronized("FI_SPECIAL", 4),
                "sellWithSynchronized should return true");

        FlashSaleItem item = repository.findById("FI_SPECIAL");
        assertEquals(5, item.getSoldQty());
        assertEquals(1, item.getVersion());
    }

    private static void assertSellWithFileLockUpdatesSoldQty() throws Exception {
        FlashSaleItemRepository repository =
                prepareFlashSaleItemRepository("flash_items_file_lock.csv", 0, 10, 1);

        assertTrue(repository.sellWithFileLock("FI_SPECIAL", 6),
                "sellWithFileLock should return true");

        FlashSaleItem item = repository.findById("FI_SPECIAL");
        assertEquals(6, item.getSoldQty());
        assertEquals(1, item.getVersion());
    }

    private static void assertSellWithOptimisticLockUpdatesSoldQtyAndVersion()
            throws Exception {

        FlashSaleItemRepository repository =
                prepareFlashSaleItemRepository("flash_items_optimistic.csv", 2, 10, 7);

        assertTrue(repository.sellWithOptimisticLock("FI_SPECIAL", 3),
                "sellWithOptimisticLock should return true");

        FlashSaleItem item = repository.findById("FI_SPECIAL");
        assertEquals(5, item.getSoldQty());
        assertEquals(8, item.getVersion());
    }

    private static void assertSellThrowsOutOfStockAndKeepsData()
            throws Exception {

        FlashSaleItemRepository repository =
                prepareFlashSaleItemRepository("flash_items_out_of_stock.csv", 4, 5, 2);

        assertThrows(OutOfStockException.class,
                () -> repository.sellWithSynchronized("FI_SPECIAL", 2));

        FlashSaleItem item = repository.findById("FI_SPECIAL");
        assertEquals(4, item.getSoldQty());
        assertEquals(2, item.getVersion());
    }

    private static FlashSaleItemRepository prepareFlashSaleItemRepository(
            String fileName,
            int soldQty,
            int limitedQty,
            int version) throws IOException {

        String filePath = testFile(fileName);
        resetFile(filePath,
                "id,createdAt,updatedAt,eventId,productId,flashPrice,limitedQty,soldQty,discountPercent,version,status");

        FlashSaleItemRepository repository =
                new FlashSaleItemRepository(filePath);

        repository.save(new FlashSaleItem(
                "FI_SPECIAL",
                CREATED_AT,
                UPDATED_AT,
                "E_SPECIAL",
                "P_SPECIAL",
                990.0,
                limitedQty,
                soldQty,
                10.0,
                version,
                SaleStatus.ACTIVE));

        return repository;
    }

    private static void testOrderRepositoryCrud() throws Exception {
        runCrudAssertions(
                new OrderRepository(testFile("orders.csv")),
                testFile("orders.csv"),
                "id,createdAt,updatedAt,customerId,eventId,totalAmount,status,lockMechanism",
                new Order("O001", CREATED_AT, UPDATED_AT,
                        "C001", "E001", 1000.0, OrderStatus.PENDING,
                        LockMechanism.NO_LOCK),
                new Order("O002", CREATED_AT, UPDATED_AT,
                        "C002", "E001", 2000.0, OrderStatus.SUCCESS,
                        LockMechanism.SYNCHRONIZED),
                new Order("O002", CREATED_AT, UPDATED_AT,
                        "C002", "E001", 2500.0, OrderStatus.SUCCESS,
                        LockMechanism.OPTIMISTIC_LOCK));
    }

    private static void testOrderDetailRepositoryCrud() throws Exception {
        runCrudAssertions(
                new OrderDetailRepository(testFile("order_details.csv")),
                testFile("order_details.csv"),
                "id,createdAt,updatedAt,orderId,flashItemId,quantity,unitPrice,subTotal",
                new OrderDetail("OD001", CREATED_AT, UPDATED_AT,
                        "O001", "FI001", 1, 800.0, 800.0),
                new OrderDetail("OD002", CREATED_AT, UPDATED_AT,
                        "O002", "FI002", 2, 1500.0, 3000.0),
                new OrderDetail("OD002", CREATED_AT, UPDATED_AT,
                        "O002", "FI002", 3, 1400.0, 4200.0));
    }

    private static void testPaymentRepositoryCrud() throws Exception {
        runCrudAssertions(
                new PaymentRepository(testFile("payments.csv")),
                testFile("payments.csv"),
                "id,createdAt,updatedAt,orderId,customerId,paymentMethod,amount",
                new Payment("PAY001", CREATED_AT, UPDATED_AT,
                        "O001", "C001", PaymentMethod.CASH, 1000.0),
                new Payment("PAY002", CREATED_AT, UPDATED_AT,
                        "O002", "C002", PaymentMethod.BANKING, 2000.0),
                new Payment("PAY002", CREATED_AT, UPDATED_AT,
                        "O002", "C002", PaymentMethod.CASH, 2500.0));
    }

    private static void testOrderTransactionRepositoryCrud() throws Exception {
        runCrudAssertions(
                new OrderTransactionRepository(testFile("transactions.csv")),
                testFile("transactions.csv"),
                "id,createdAt,updatedAt,orderId,threadName,mechanism,success,retryCount,executionTimeMs,message",
                new OrderTransaction("TX001", CREATED_AT, UPDATED_AT,
                        "O001", "Thread-1", LockMechanism.NO_LOCK,
                        false, 1, 120L, "failed"),
                new OrderTransaction("TX002", CREATED_AT, UPDATED_AT,
                        "O002", "Thread-2", LockMechanism.SYNCHRONIZED,
                        true, 0, 80L, "ok"),
                new OrderTransaction("TX002", CREATED_AT, UPDATED_AT,
                        "O002", "Thread-2", LockMechanism.OPTIMISTIC_LOCK,
                        true, 1, 95L, "ok updated"));
    }

    private static void testUserRepositoryCrud() throws Exception {
        runCrudAssertions(
                new UserRepository(testFile("users.csv")),
                testFile("users.csv"),
                "id,createdAt,updatedAt,username,passwordHash,role,active",
                new User("U001", CREATED_AT, UPDATED_AT,
                        "user1", "$2a$10$hash1", UserRole.CUSTOMER, true),
                new User("U002", CREATED_AT, UPDATED_AT,
                        "seller1", "$2a$10$hash2", UserRole.SELLER, true),
                new User("U002", CREATED_AT, UPDATED_AT,
                        "admin1", "$2a$10$hash3", UserRole.ADMIN, false));
    }

    private static void testReadTenThousandRowsUnderOneSecond() throws Exception {
        String filePath = testFile("products_10k.csv");
        writeProductRows(filePath, 10_000);

        ProductRepository repository = new ProductRepository(filePath);

        long startNs = System.nanoTime();
        List<Product> products = repository.findAll();
        long elapsedNs = System.nanoTime() - startNs;

        int rowCounter = products.size();
        long elapsedMs = elapsedNs / 1_000_000;
        double rowsPerSecond = rowCounter / (elapsedNs / 1_000_000_000.0);

        System.out.println("Rows read   : " + rowCounter);
        System.out.println("Elapsed ms  : " + elapsedMs);
        System.out.println("Rows/second : " + String.format("%.2f", rowsPerSecond));

        assertEquals(10_000, rowCounter);
        assertTrue(elapsedMs <= 1000,
                "Expected reading 10,000 rows within 1 second but took "
                        + elapsedMs + " ms");
    }

    private static <T extends BaseEntity> void runCrudAssertions(
            CsvRepository<T> repository,
            String filePath,
            String header,
            T firstEntity,
            T secondEntity,
            T updatedSecondEntity) throws Exception {

        resetFile(filePath, header);

        repository.save(firstEntity);
        assertEquals(1, repository.findAll().size());
        assertCsvLine(firstEntity, repository.findById(firstEntity.getId()));

        repository.save(secondEntity);
        assertEquals(2, repository.findAll().size());
        assertCsvLine(secondEntity, repository.findById(secondEntity.getId()));

        assertTrue(repository.update(updatedSecondEntity),
                "Update should return true for existing id " + updatedSecondEntity.getId());
        assertCsvLine(updatedSecondEntity, repository.findById(updatedSecondEntity.getId()));

        assertTrue(repository.delete(firstEntity.getId()),
                "Delete should return true for existing id " + firstEntity.getId());
        assertEquals(null, repository.findById(firstEntity.getId()));
        assertEquals(1, repository.findAll().size());

        assertTrue(!repository.delete("MISSING_ID"),
                "Delete should return false for missing id");
    }

    private static void resetFile(String filePath, String header) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(filePath))) {
            writer.write(header);
            writer.newLine();
        }
    }

    private static void writeProductRows(String filePath, int count) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(filePath))) {
            writer.write("id,createdAt,updatedAt,name,category,originalPrice,stockQty,version,status");
            writer.newLine();

            for (int i = 1; i <= count; i++) {
                Product product = new Product(
                        "P" + String.format("%05d", i),
                        CREATED_AT,
                        UPDATED_AT,
                        "Product " + i,
                        "Category",
                        1000.0 + i,
                        100 + i,
                        1,
                        SaleStatus.ACTIVE);

                writer.write(product.toCsvLine());
                writer.newLine();
            }
        }
    }

    private static String testFile(String fileName) {
        return TEST_DIR.resolve(fileName).toString();
    }

    private static void cleanupTestFiles() {
        try {
            if (Files.exists(TEST_DIR)) {
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(TEST_DIR)) {
                    for (Path path : stream) {
                        deleteWithRetry(path);
                    }
                }
            }

            deleteWithRetry(TEST_DIR);
        } catch (IOException e) {
            System.out.println("[WARN] Cannot clean test files: " + e.getMessage());
        }
    }

    private static void deleteWithRetry(Path path) throws IOException {
        IOException lastError = null;

        for (int i = 0; i < 5; i++) {
            try {
                Files.deleteIfExists(path);
                return;
            } catch (IOException e) {
                lastError = e;

                try {
                    Thread.sleep(100);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    throw e;
                }
            }
        }

        throw lastError;
    }

    private static void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void assertCsvLine(BaseEntity expected, BaseEntity actual) {
        if (actual == null) {
            throw new AssertionError("Expected entity " + expected.getId() + " but was null");
        }

        assertEquals(expected.toCsvLine(), actual.toCsvLine());
    }

    private static void runTest(String testName, TestCase testCase) {
        totalTests++;
        System.out.println("========================================");
        System.out.println("TEST: " + testName);

        try {
            testCase.run();
            passedTests++;
            System.out.println("RESULT: PASS");
        } catch (Throwable error) {
            System.out.println("RESULT: FAIL");
            System.out.println("ERROR : " + error.getMessage());
        }
    }

    private static void assertEquals(Object expected, Object actual) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError("Expected <" + expected + "> but was <" + actual + ">");
        }
    }

    private static void assertEquals(int expected, int actual) {
        if (expected != actual) {
            throw new AssertionError("Expected <" + expected + "> but was <" + actual + ">");
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertThrows(
            Class<? extends Throwable> expectedType,
            TestCase testCase) throws Exception {

        try {
            testCase.run();
        } catch (Throwable actual) {
            if (expectedType.isInstance(actual)) {
                return;
            }

            throw new AssertionError("Expected exception <"
                    + expectedType.getSimpleName()
                    + "> but was <"
                    + actual.getClass().getSimpleName()
                    + ">");
        }

        throw new AssertionError("Expected exception <"
                + expectedType.getSimpleName()
                + "> but nothing was thrown");
    }

    private interface TestCase {
        void run() throws Exception;
    }

    private static class RepositoryConsole {
        private final String key;
        private final String displayName;
        private final String fileName;
        private final String header;
        private final CsvRepository repository;

        private RepositoryConsole(
                String key,
                String displayName,
                String fileName,
                String header,
                CsvRepository repository) {

            this.key = key;
            this.displayName = displayName;
            this.fileName = fileName;
            this.header = header;
            this.repository = repository;
        }
    }
}
