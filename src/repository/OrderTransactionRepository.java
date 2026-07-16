package repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import model.Entity.OrderTransaction;

public class OrderTransactionRepository extends CsvRepository<OrderTransaction> {

    public static final String BENCHMARK_HEADER =
            "transactionId,threadId,mechanism,success,retryCount,executionTimeMs,"
            + "stockBefore,stockAfter,versionBefore,versionAfter,timestamp,message";

    public OrderTransactionRepository() {
        super("data/transactions.csv");
    }

    public OrderTransactionRepository(String filePath) {
        super(filePath);
    }
    // --- Quản lý Event ---

    @Override
    protected OrderTransaction mapFromCsv(String csvLine) {

        OrderTransaction orderTransaction = new OrderTransaction();

        try {
            orderTransaction.fromCsvLine(csvLine);
            return orderTransaction;
        } catch (Exception e) {
            return null;
        }
    }

    public void ensureBenchmarkHeader() {
        Path path = Paths.get(filePath);
        List<String> lines = new ArrayList<String>();

        try {
            if (Files.exists(path)) {
                lines.addAll(Files.readAllLines(path));
            }

            if (!lines.isEmpty() && BENCHMARK_HEADER.equals(lines.get(0))) {
                return;
            }

            if (lines.isEmpty()) {
                lines.add(BENCHMARK_HEADER);
            } else {
                lines.set(0, BENCHMARK_HEADER);
            }

            Files.write(path, lines);
        } catch (IOException e) {
            System.out.println("[ERROR] Unable to prepare transaction header: " + e.getMessage());
        }
    }

    public void clearFile() {
        try {
            ensureBenchmarkHeader();
            Path path = Paths.get(filePath);
            Files.write(path, Arrays.asList(BENCHMARK_HEADER));
        } catch (IOException e) {
            System.out.println("[ERROR] Unable to clear transaction file: " + e.getMessage());
        }
    }
}
    

    
