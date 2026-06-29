package test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

public class RepositoryCrudJUnitTest {

    private static final LocalDateTime CREATED_AT =
            LocalDateTime.of(2026, 1, 2, 3, 4, 5);
    private static final LocalDateTime UPDATED_AT =
            LocalDateTime.of(2026, 2, 3, 4, 5, 6);
    private static final Path TEST_DIR =
            Paths.get("data", "test_repository_crud_junit");

    @BeforeEach
    void createTestDirectory() throws IOException {
        Files.createDirectories(TEST_DIR);
        cleanTestDirectory();
    }

    @AfterEach
    void deleteTestDirectory() throws IOException {
        cleanTestDirectory();
        Files.deleteIfExists(TEST_DIR);
    }

    @Test
    void customerRepositoryCrud() throws Exception {
        runCrudAssertions(
                new CustomerRepository(testFile("customers.csv")),
                testFile("customers.csv"),
                "customers.csv",
                new Customer("JUNIT_C001", CREATED_AT, UPDATED_AT,
                        "JUNIT_U001", "Nguyen Van A", "0909123456", "a@example.com",
                        CustomerTier.NORMAL, 1000.0, true),
                new Customer("JUNIT_C002", CREATED_AT, UPDATED_AT,
                        "JUNIT_U002", "Tran Thi B", "0911222333", "b@example.com",
                        CustomerTier.VIP, 2000.0, true),
                new Customer("JUNIT_C002", CREATED_AT, UPDATED_AT,
                        "JUNIT_U002", "Tran Thi B Updated", "0911222333", "b@example.com",
                        CustomerTier.PREMIUM, 3000.0, false));
    }

    @Test
    void productRepositoryCrud() throws Exception {
        runCrudAssertions(
                new ProductRepository(testFile("products.csv")),
                testFile("products.csv"),
                "products.csv",
                new Product("JUNIT_P001", CREATED_AT, UPDATED_AT,
                        "Laptop A", "Laptop", 1000.0, 20, 1,
                        SaleStatus.ACTIVE),
                new Product("JUNIT_P002", CREATED_AT, UPDATED_AT,
                        "Phone B", "Smartphone", 2000.0, 30, 1,
                        SaleStatus.ACTIVE),
                new Product("JUNIT_P002", CREATED_AT, UPDATED_AT,
                        "Phone B Updated", "Smartphone", 2500.0, 25, 2,
                        SaleStatus.DISABLED));
    }

    @Test
    void flashSaleRepositoryCrud() throws Exception {
        runCrudAssertions(
                new FlashSaleRepository(testFile("flash_events.csv")),
                testFile("flash_events.csv"),
                "flash_events.csv",
                new FlashSaleEvent("JUNIT_E001", CREATED_AT, UPDATED_AT,
                        "Morning Sale",
                        LocalDateTime.of(2026, 3, 1, 8, 0, 1),
                        LocalDateTime.of(2026, 3, 1, 10, 0, 1),
                        SaleStatus.UPCOMING),
                new FlashSaleEvent("JUNIT_E002", CREATED_AT, UPDATED_AT,
                        "Noon Sale",
                        LocalDateTime.of(2026, 3, 2, 12, 0, 1),
                        LocalDateTime.of(2026, 3, 2, 14, 0, 1),
                        SaleStatus.ACTIVE),
                new FlashSaleEvent("JUNIT_E002", CREATED_AT, UPDATED_AT,
                        "Noon Sale Updated",
                        LocalDateTime.of(2026, 3, 2, 12, 0, 1),
                        LocalDateTime.of(2026, 3, 2, 15, 0, 1),
                        SaleStatus.ENDED));
    }

    @Test
    void flashSaleItemRepositoryCrud() throws Exception {
        runCrudAssertions(
                new FlashSaleItemRepository(testFile("flash_items.csv")),
                testFile("flash_items.csv"),
                "flash_items.csv",
                new FlashSaleItem("JUNIT_FI001", CREATED_AT, UPDATED_AT,
                        "JUNIT_E001", "JUNIT_P001", 800.0, 100, 0, 20.0, 1,
                        SaleStatus.ACTIVE),
                new FlashSaleItem("JUNIT_FI002", CREATED_AT, UPDATED_AT,
                        "JUNIT_E001", "JUNIT_P002", 1500.0, 50, 5, 25.0, 1,
                        SaleStatus.ACTIVE),
                new FlashSaleItem("JUNIT_FI002", CREATED_AT, UPDATED_AT,
                        "JUNIT_E001", "JUNIT_P002", 1400.0, 50, 10, 30.0, 2,
                        SaleStatus.ACTIVE));
    }

    @Test
    void orderRepositoryCrud() throws Exception {
        runCrudAssertions(
                new OrderRepository(testFile("orders.csv")),
                testFile("orders.csv"),
                "orders.csv",
                new Order("JUNIT_O001", CREATED_AT, UPDATED_AT,
                        "JUNIT_C001", 1000.0, OrderStatus.PENDING,
                        LockMechanism.NO_LOCK),
                new Order("JUNIT_O002", CREATED_AT, UPDATED_AT,
                        "JUNIT_C002", 2000.0, OrderStatus.SUCCESS,
                        LockMechanism.SYNCHRONIZED),
                new Order("JUNIT_O002", CREATED_AT, UPDATED_AT,
                        "JUNIT_C002",  2500.0, OrderStatus.SUCCESS,
                        LockMechanism.OPTIMISTIC_LOCK));
    }

    @Test
    void orderDetailRepositoryCrud() throws Exception {
        runCrudAssertions(
                new OrderDetailRepository(testFile("order_details.csv")),
                testFile("order_details.csv"),
                "order_details.csv",
                new OrderDetail("JUNIT_OD001", CREATED_AT, UPDATED_AT,
                        "JUNIT_O001", "JUNIT_FI001", 1, 800.0, 800.0),
                new OrderDetail("JUNIT_OD002", CREATED_AT, UPDATED_AT,
                        "JUNIT_O002", "JUNIT_FI002", 2, 1500.0, 3000.0),
                new OrderDetail("JUNIT_OD002", CREATED_AT, UPDATED_AT,
                        "JUNIT_O002", "JUNIT_FI002", 3, 1400.0, 4200.0));
    }

    @Test
    void paymentRepositoryCrud() throws Exception {
        runCrudAssertions(
                new PaymentRepository(testFile("payments.csv")),
                testFile("payments.csv"),
                "payments.csv",
                new Payment("JUNIT_PAY001", CREATED_AT, UPDATED_AT,
                        "JUNIT_O001", "JUNIT_C001", PaymentMethod.CASH, 1000.0),
                new Payment("JUNIT_PAY002", CREATED_AT, UPDATED_AT,
                        "JUNIT_O002", "JUNIT_C002", PaymentMethod.BANKING, 2000.0),
                new Payment("JUNIT_PAY002", CREATED_AT, UPDATED_AT,
                        "JUNIT_O002", "JUNIT_C002", PaymentMethod.CASH, 2500.0));
    }

    @Test
    void orderTransactionRepositoryCrud() throws Exception {
        runCrudAssertions(
                new OrderTransactionRepository(testFile("transactions.csv")),
                testFile("transactions.csv"),
                "transactions.csv",
                new OrderTransaction("JUNIT_TX001", CREATED_AT, UPDATED_AT,
                        "JUNIT_O001", "Thread-1", LockMechanism.NO_LOCK,
                        false, 1, 120L, "failed"),
                new OrderTransaction("JUNIT_TX002", CREATED_AT, UPDATED_AT,
                        "JUNIT_O002", "Thread-2", LockMechanism.SYNCHRONIZED,
                        true, 0, 80L, "ok"),
                new OrderTransaction("JUNIT_TX002", CREATED_AT, UPDATED_AT,
                        "JUNIT_O002", "Thread-2", LockMechanism.OPTIMISTIC_LOCK,
                        true, 1, 95L, "ok updated"));
    }

    @Test
    void userRepositoryCrud() throws Exception {
        runCrudAssertions(
                new UserRepository(testFile("users.csv")),
                testFile("users.csv"),
                "users.csv",
                new User("JUNIT_U001", CREATED_AT, UPDATED_AT,
                        "user1", "$2a$10$hash1", UserRole.CUSTOMER, true),
                new User("JUNIT_U002", CREATED_AT, UPDATED_AT,
                        "seller1", "$2a$10$hash2", UserRole.SELLER, true),
                new User("JUNIT_U002", CREATED_AT, UPDATED_AT,
                        "admin1", "$2a$10$hash3", UserRole.ADMIN, false));
    }

    @Test
    void repositoriesReadAllMainCsvFilesUnderOneSecond()
            throws Exception {

        long startNs = System.nanoTime();

        int customerRows = measureRead(
                "customers.csv",
                new RowReader() {
                    @Override
                    public int read() {
                        return new CustomerRepository("data/customers.csv")
                                .findAll()
                                .size();
                    }
                }
        );

        int productRows = measureRead(
                "products.csv",
                new RowReader() {
                    @Override
                    public int read() {
                        return new ProductRepository("data/products.csv")
                                .findAll()
                                .size();
                    }
                }
        );

        int eventRows = measureRead(
                "flash_events.csv",
                new RowReader() {
                    @Override
                    public int read() {
                        return new FlashSaleRepository("data/flash_events.csv")
                                .findAll()
                                .size();
                    }
                }
        );

        int flashItemRows = measureRead(
                "flash_items.csv",
                new RowReader() {
                    @Override
                    public int read() {
                        return new FlashSaleItemRepository("data/flash_items.csv")
                                .findAll()
                                .size();
                    }
                }
        );

        int orderRows = measureRead(
                "orders.csv",
                new RowReader() {
                    @Override
                    public int read() {
                        return new OrderRepository("data/orders.csv")
                                .findAll()
                                .size();
                    }
                }
        );

        int orderDetailRows = measureRead(
                "order_details.csv",
                new RowReader() {
                    @Override
                    public int read() {
                        return new OrderDetailRepository("data/order_details.csv")
                                .findAll()
                                .size();
                    }
                }
        );

        int paymentRows = measureRead(
                "payments.csv",
                new RowReader() {
                    @Override
                    public int read() {
                        return new PaymentRepository("data/payments.csv")
                                .findAll()
                                .size();
                    }
                }
        );

        int transactionRows = measureRead(
                "transactions.csv",
                new RowReader() {
                    @Override
                    public int read() {
                        return new OrderTransactionRepository("data/transactions.csv")
                                .findAll()
                                .size();
                    }
                }
        );

        int userRows = measureRead(
                "users.csv",
                new RowReader() {
                    @Override
                    public int read() {
                        return new UserRepository("data/users.csv")
                                .findAll()
                                .size();
                    }
                }
        );

        long elapsedNs = System.nanoTime() - startNs;
        long elapsedMs = elapsedNs / 1_000_000;
        int totalRows = customerRows
                + productRows
                + eventRows
                + flashItemRows
                + orderRows
                + orderDetailRows
                + paymentRows
                + transactionRows
                + userRows;
        double rowsPerSecond = totalRows / (elapsedNs / 1_000_000_000.0);

        System.out.println("Total rows read   : " + totalRows);
        System.out.println("Total elapsed ms  : " + elapsedMs);
        System.out.println("Total rows/second : "
                + String.format("%.2f", rowsPerSecond));

        assertTrue(totalRows > 0, "Expected at least one row across CSV files");
        assertTrue(elapsedMs <= 1000,
                "Expected reading all CSV files within 1 second but took "
                        + elapsedMs + " ms");
    }

    private static <T extends BaseEntity> void runCrudAssertions(
            CsvRepository<T> repository,
            String filePath,
            String sourceFileName,
            T firstEntity,
            T secondEntity,
            T updatedSecondEntity
    ) throws Exception {

        copyMainDataFile(sourceFileName, filePath);

        int initialSize = repository.findAll().size();
        assertNull(repository.findById(firstEntity.getId()),
                "Test id should not already exist in copied data: "
                        + firstEntity.getId());
        assertNull(repository.findById(secondEntity.getId()),
                "Test id should not already exist in copied data: "
                        + secondEntity.getId());

        repository.save(firstEntity);
        assertEquals(initialSize + 1, repository.findAll().size());
        assertCsvLine(firstEntity, repository.findById(firstEntity.getId()));

        repository.save(secondEntity);
        assertEquals(initialSize + 2, repository.findAll().size());
        assertCsvLine(secondEntity, repository.findById(secondEntity.getId()));

        assertTrue(repository.update(updatedSecondEntity),
                "Update should return true for existing id "
                        + updatedSecondEntity.getId());
        assertCsvLine(updatedSecondEntity,
                repository.findById(updatedSecondEntity.getId()));

        assertTrue(repository.delete(firstEntity.getId()),
                "Delete should return true for existing id "
                        + firstEntity.getId());
        assertNull(repository.findById(firstEntity.getId()));
        assertEquals(initialSize + 1, repository.findAll().size());

        assertFalse(repository.delete("MISSING_ID"),
                "Delete should return false for missing id");
    }

    private static void assertCsvLine(BaseEntity expected, BaseEntity actual) {
        assertNotNull(actual, "Expected entity " + expected.getId());
        assertEquals(expected.toCsvLine(), actual.toCsvLine());
    }

    private static void copyMainDataFile(String sourceFileName, String targetFilePath)
            throws IOException {

        Files.copy(
                Paths.get("data", sourceFileName),
                Paths.get(targetFilePath),
                StandardCopyOption.REPLACE_EXISTING
        );
    }

    

    private static String testFile(String fileName) {
        return TEST_DIR.resolve(fileName).toString();
    }

    private static int measureRead(String fileName, RowReader reader) {
        long startNs = System.nanoTime();
        int rows = reader.read();
        long elapsedMs = (System.nanoTime() - startNs) / 1_000_000;

        System.out.println(fileName + " rows=" + rows
                + ", elapsedMs=" + elapsedMs);

        return rows;
    }

    private interface RowReader {
        int read();
    }

    private static void cleanTestDirectory() throws IOException {
        if (!Files.exists(TEST_DIR)) {
            return;
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(TEST_DIR)) {
            for (Path path : stream) {
                Files.deleteIfExists(path);
            }
        }
    }
}
