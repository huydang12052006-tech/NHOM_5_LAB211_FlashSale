import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import model.BaseEntity.BaseEntity;
import repository.CsvRepository;
import repository.CustomerRepository;
import repository.FlashSaleItemRepository;
import repository.FlashSaleRepository;
import repository.OrderDetailRepository;
import repository.OrderRepository;
import repository.ProductRepository;

public class TestCsvParser {

    private static final int SAMPLE_SIZE = 3;

    public static void main(String[] args) {
        testRawCsvFile("data/products.csv");
        testRawCsvFile("data/customers.csv");
        testRawCsvFile("data/flash_events.csv");
        testRawCsvFile("data/flash_items.csv");
        testRawCsvFile("data/orders.csv");
        testRawCsvFile("data/order_details.csv");

        testRepository("Products", new ProductRepository());
        testRepository("Customers", new CustomerRepository());
        testRepository("Flash sale events", new FlashSaleRepository());
        testRepository("Flash sale items", new FlashSaleItemRepository());
        testRepository("Orders", new OrderRepository());
        testRepository("Order details", new OrderDetailRepository());
    }

    private static void testRawCsvFile(String filePath) {
        System.out.println("========================================");
        System.out.println("RAW CSV FILE: " + filePath);

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String header = reader.readLine();

            if (header == null) {
                System.out.println("File is empty.");
                return;
            }

            String[] columns = parseCsvLine(header);
            System.out.println("Column count: " + columns.length);
            System.out.println("Columns     : " + Arrays.toString(columns));

            int rowCount = 0;
            String line;

            while ((line = reader.readLine()) != null && rowCount < SAMPLE_SIZE) {
                String[] values = parseCsvLine(line);
                System.out.println("Row " + (rowCount + 1) + " (" + values.length + " cols): "
                        + Arrays.toString(values));
                rowCount++;
            }
        } catch (IOException e) {
            System.out.println("[ERROR] Cannot read file: " + e.getMessage());
        }
    }

    private static String[] parseCsvLine(String line) {
        return line.split(",", -1);
    }

    private static <T extends BaseEntity> void testRepository(
            String name,
            CsvRepository<T> repository) {

        System.out.println("========================================");
        System.out.println("TEST CSV: " + name);

        List<T> records = repository.findAll();

        long parsedCount = records.stream()
                .filter(Objects::nonNull)
                .count();

        long errorCount = records.size() - parsedCount;

        System.out.println("Total lines read : " + records.size());
        System.out.println("Parsed OK        : " + parsedCount);
        System.out.println("Parse error/null : " + errorCount);

        if (parsedCount == 0) {
            System.out.println("No valid record parsed.");
            return;
        }

        System.out.println("Sample records:");

        int printed = 0;
        for (T record : records) {
            if (record == null) {
                continue;
            }

            System.out.println(record);
            printed++;

            if (printed == SAMPLE_SIZE) {
                break;
            }
        }
    }
}
