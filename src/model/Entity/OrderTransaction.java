package model.Entity;

import model.Enum.LockMechanism;

import java.time.LocalDateTime;

import model.BaseEntity.BaseEntity;


public class OrderTransaction extends BaseEntity {

    private String orderId;

    private String threadName;

    private LockMechanism mechanism;

    private boolean success;

    private int retryCount;

    private long executionTimeMs;

    private String message;

    // =========================
    // Constructors
    // =========================

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

        super(id,createdAt,updatedAt);

        this.orderId = orderId;
        this.threadName = threadName;
        this.mechanism = mechanism;
        this.success = success;
        this.retryCount = retryCount;
        this.executionTimeMs = executionTimeMs;
        this.message = message;
    }

    // =========================
    // Getter & Setter
    // =========================

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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    // =========================
    // CSV Methods
    // =========================

    @Override
    public String toCsvLine() {

        return String.join(",",
                escapeCsv(getId()),
                escapeCsv(orderId),
                escapeCsv(threadName),
                mechanism.name(),
                String.valueOf(success),
                String.valueOf(retryCount),
                String.valueOf(executionTimeMs),
                escapeCsv(message),
                formatDateTime(getCreatedAt()),
                formatDateTime(getUpdatedAt())
        );
    }

    @Override
    public void fromCsvLine(String csv) {

        String[] parts = csv.split(",", -1);

        setId(parts[0]);

        this.orderId = parts[3];

        this.threadName = parts[4];

        this.mechanism =
                LockMechanism.valueOf(parts[5]);

        this.success =
                Boolean.parseBoolean(parts[6]);

        this.retryCount =
                Integer.parseInt(parts[7]);

        this.executionTimeMs =
                Long.parseLong(parts[8]);

        this.message = parts[9];

        setCreatedAt(
                parseDateTime(parts[1])
        );

        setUpdatedAt(
                parseDateTime(parts[2])
        );
    }

    @Override
    public String toString() {

        return "OrderTransaction{" +
                "id='" + getId() + '\'' +
                ", orderId='" + orderId + '\'' +
                ", threadName='" + threadName + '\'' +
                ", mechanism=" + mechanism +
                ", success=" + success +
                ", retryCount=" + retryCount +
                ", executionTimeMs=" + executionTimeMs +
                ", message='" + message + '\'' +
                '}';
    }
}