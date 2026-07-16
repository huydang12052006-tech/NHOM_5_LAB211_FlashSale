package model.Entity;

import java.time.LocalDateTime;
import model.BaseEntity.BaseEntity;
import model.Enum.LockMechanism;

public class OrderTransaction extends BaseEntity {

    private String orderId;
    private String threadName;
    private LockMechanism mechanism;
    private boolean success;
    private int retryCount;
    private long executionTimeMs;
    private long negativeWriteTimeMs;
    private int stockBefore;
    private int stockAfter;
    private int versionBefore;
    private int versionAfter;
    private String message;

    public OrderTransaction() {
    }

    public OrderTransaction(String id,
                            LocalDateTime createdAt,
                            LocalDateTime updatedAt,
                            String orderId,
                            String threadName,
                            LockMechanism mechanism,
                            boolean success,
                            int retryCount,
                            long executionTimeMs,
                            String message) {
        this(
                id,
                createdAt,
                updatedAt,
                orderId,
                threadName,
                mechanism,
                success,
                retryCount,
                executionTimeMs,
                0L,
                -1,
                -1,
                -1,
                -1,
                message
        );
    }

    public OrderTransaction(String id,
                            LocalDateTime createdAt,
                            LocalDateTime updatedAt,
                            String orderId,
                            String threadName,
                            LockMechanism mechanism,
                            boolean success,
                            int retryCount,
                            long executionTimeMs,
                            long negativeWriteTimeMs,
                            int stockBefore,
                            int stockAfter,
                            int versionBefore,
                            int versionAfter,
                            String message) {
        super(id, createdAt, updatedAt);
        this.orderId = orderId;
        this.threadName = threadName;
        this.mechanism = mechanism;
        this.success = success;
        this.retryCount = retryCount;
        this.executionTimeMs = executionTimeMs;
        this.negativeWriteTimeMs = negativeWriteTimeMs;
        this.stockBefore = stockBefore;
        this.stockAfter = stockAfter;
        this.versionBefore = versionBefore;
        this.versionAfter = versionAfter;
        this.message = message;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public LockMechanism getMechanism() {
        return mechanism;
    }

    public void setMechanism(LockMechanism mechanism) {
        this.mechanism = mechanism;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }

    public long getNegativeWriteTimeMs() {
        return negativeWriteTimeMs;
    }

    public void setNegativeWriteTimeMs(long negativeWriteTimeMs) {
        this.negativeWriteTimeMs = negativeWriteTimeMs;
    }

    public int getStockBefore() {
        return stockBefore;
    }

    public void setStockBefore(int stockBefore) {
        this.stockBefore = stockBefore;
    }

    public int getStockAfter() {
        return stockAfter;
    }

    public void setStockAfter(int stockAfter) {
        this.stockAfter = stockAfter;
    }

    public int getVersionBefore() {
        return versionBefore;
    }

    public void setVersionBefore(int versionBefore) {
        this.versionBefore = versionBefore;
    }

    public int getVersionAfter() {
        return versionAfter;
    }

    public void setVersionAfter(int versionAfter) {
        this.versionAfter = versionAfter;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toCsvLine() {
        return String.join(",",
                escapeCsv(getId()),
                escapeCsv(threadName),
                mechanism.name(),
                String.valueOf(success),
                String.valueOf(retryCount),
                String.valueOf(executionTimeMs),
                String.valueOf(negativeWriteTimeMs),
                String.valueOf(stockBefore),
                String.valueOf(stockAfter),
                String.valueOf(versionBefore),
                String.valueOf(versionAfter),
                formatDateTime(getCreatedAt()),
                escapeCsv(message)
        );
    }

    @Override
    public void fromCsvLine(String csv) {
        String[] parts = csv.split(",", -1);

        if (parts.length >= 12 && !isDateTime(parts[1])) {
            parseBenchmarkCsv(parts);
            return;
        }

        parseLegacyCsv(parts);
    }

    private void parseBenchmarkCsv(String[] parts) {
        setId(parts[0]);
        this.threadName = parts[1];
        this.mechanism = LockMechanism.valueOf(parts[2]);
        this.success = Boolean.parseBoolean(parts[3]);
        this.retryCount = Integer.parseInt(parts[4]);
        this.executionTimeMs = Long.parseLong(parts[5]);
        this.negativeWriteTimeMs = parts.length > 6 ? Long.parseLong(parts[6]) : 0L;
        this.stockBefore = Integer.parseInt(parts[7]);
        this.stockAfter = Integer.parseInt(parts[8]);
        this.versionBefore = Integer.parseInt(parts[9]);
        this.versionAfter = Integer.parseInt(parts[10]);
        setCreatedAt(LocalDateTime.parse(parts[11]));
        setUpdatedAt(getCreatedAt());
        this.message = parts[12];
        this.orderId = "";
    }

    private void parseLegacyCsv(String[] parts) {
        setId(parts[0]);
        setCreatedAt(LocalDateTime.parse(parts[1]));
        setUpdatedAt(LocalDateTime.parse(parts[2]));
        this.orderId = parts[3];
        this.threadName = parts[4];
        this.mechanism = LockMechanism.valueOf(parts[5]);
        this.success = Boolean.parseBoolean(parts[6]);
        this.retryCount = Integer.parseInt(parts[7]);
        this.executionTimeMs = Long.parseLong(parts[8]);
        this.negativeWriteTimeMs = 0L;
        this.stockBefore = -1;
        this.stockAfter = -1;
        this.versionBefore = -1;
        this.versionAfter = -1;
        this.message = parts[9];
    }

    private boolean isDateTime(String value) {
        try {
            LocalDateTime.parse(value);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    @Override
    public String toString() {
        return "OrderTransaction{" +
                "id='" + getId() + '\'' +
                ", threadName='" + threadName + '\'' +
                ", mechanism=" + mechanism +
                ", success=" + success +
                ", retryCount=" + retryCount +
                ", executionTimeMs=" + executionTimeMs +
                ", stockBefore=" + stockBefore +
                ", stockAfter=" + stockAfter +
                ", versionBefore=" + versionBefore +
                ", versionAfter=" + versionAfter +
                ", message='" + message + '\'' +
                '}';
    }
}
